package com.Twitter.Jarvis.ServiceImpl;

import com.Twitter.Jarvis.Config.JwtProvider;
import com.Twitter.Jarvis.Exception.UserException;
import com.Twitter.Jarvis.Model.UserConfigurationModel;
import com.Twitter.Jarvis.Repository.UserRepository;
import com.Twitter.Jarvis.Response.AuthResponse;
import com.Twitter.Jarvis.Service.CustomUserDetailsServiceImpl;
import com.Twitter.Jarvis.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    final static String GOOGLE_AUTH_URL = "https://oauth2.googleapis.com/token";
    final static String GITHUB_AUTH_URL = "https://github.com/login/oauth/access_token";
    final static String REDIRECT_URL = "https://www.edu.thinkvil.com/oauth2/authorization/google";
    final static String GITHUB_REDIRECT_URL = "https://www.edu.thinkvil.com/oauth2/authorization/github";

    final static String USER_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";
    final static String GITHUB_USER_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";
    @Value("${google.client.id}")
    private String client_id;

    @Value("${google.client.secret}")
    private String client_secret;

    @Value("${github.client.id}")
    private String githubClientId;

    @Value("${github.client.secret}")
    private String githubClientSecret;

    @Autowired
    private CustomUserDetailsServiceImpl customUserDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;



    @Override
    public ResponseEntity<?> redirectToProvider(String code) throws UserException {

        try {

            //1. Exchange auth code for tokens
            String tokenEndpoint = GOOGLE_AUTH_URL;

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", client_id);
            params.add("client_secret", client_secret);
            params.add("redirect_uri", REDIRECT_URL);
            params.add("grant_type", "authorization_code");

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, httpHeaders);

            ResponseEntity<Map> mapResponseEntity = restTemplate.postForEntity(tokenEndpoint, request, Map.class);
            String token = (String) mapResponseEntity.getBody().get("id_token");

            ResponseEntity<Map> userInfoResponse = restTemplate.getForEntity(USER_INFO_URL+token, Map.class);

            if(userInfoResponse.getStatusCode().equals(HttpStatus.OK)) {
                Map<String, Object> userInfo = userInfoResponse.getBody();
                String email = (String) userInfo.get("email");
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                if(userDetails == null) {
                    UserConfigurationModel user = new UserConfigurationModel();
                    user.setEmail(email);
                    user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    userRepository.save(user);
                    userDetails = customUserDetailsService.loadUserByUsername(email);

                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null,
                                userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                String jwtToken = jwtProvider.generateToken(authentication);
                AuthResponse res = new AuthResponse(token, true);

                // Redirect to frontend with the token
                String frontendRedirectUrl = "https://www.thinkvil.com/auth/callback?token=" + jwtToken;
                HttpHeaders headers = new HttpHeaders();
                headers.setLocation(URI.create(frontendRedirectUrl));
                return new ResponseEntity<>(headers, HttpStatus.FOUND);

            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        } catch (Exception e) {

            log.error("Exception occured while handleGoogleCallback ", e);

        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @Override
    public ResponseEntity<?> redirectToProviderGithub(String code) throws UserException {
        try {
            // 1. Exchange authorization code for access token
            String tokenEndpoint = GITHUB_AUTH_URL;

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", githubClientId);
            params.add("client_secret", githubClientSecret);
            params.add("redirect_uri", GITHUB_REDIRECT_URL);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON)); // GitHub returns JSON when requested

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenEndpoint, request, Map.class);

            if (!tokenResponse.getStatusCode().is2xxSuccessful() || !tokenResponse.getBody().containsKey("access_token")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to fetch access token from GitHub");
            }

            String accessToken = (String) tokenResponse.getBody().get("access_token");

            // 2. Use access token to fetch user info from GitHub
            String userInfoUrl = "https://api.github.com/user";

            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(accessToken);
            HttpEntity<String> userRequest = new HttpEntity<>(userHeaders);

            ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                    userInfoUrl,
                    HttpMethod.GET,
                    userRequest,
                    Map.class
            );

            if (userInfoResponse.getStatusCode() != HttpStatus.OK) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to fetch user info from GitHub");
            }

            Map<String, Object> userInfo = userInfoResponse.getBody();
            String email = (String) userInfo.get("email");

            // GitHub doesn't always return public email, so you may need to fetch from a secondary endpoint
            if (email == null) {
                ResponseEntity<List> emailsResponse = restTemplate.exchange(
                        "https://api.github.com/user/emails",
                        HttpMethod.GET,
                        userRequest,
                        List.class
                );

                if (emailsResponse.getStatusCode() == HttpStatus.OK) {
                    List<Map<String, Object>> emails = emailsResponse.getBody();
                    if (emails != null && !emails.isEmpty()) {
                        email = (String) emails.get(0).get("email");
                    }
                }
            }

            if (email == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email not available from GitHub account");
            }

            // 3. Load or create user based on email
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

            if (userDetails == null) {
                UserConfigurationModel user = new UserConfigurationModel();
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Random password
                userRepository.save(user);
                userDetails = customUserDetailsService.loadUserByUsername(email);
            }

            // 4. Authenticate user manually
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 5. Generate JWT
            String jwtToken = jwtProvider.generateToken(authentication);

            // 6. Redirect to frontend with token
            String frontendRedirectUrl = "https://www.thinkvil.com/auth/callback?token=" + jwtToken;
            HttpHeaders redirectHeaders = new HttpHeaders();
            redirectHeaders.setLocation(URI.create(frontendRedirectUrl));

            return new ResponseEntity<>(redirectHeaders, HttpStatus.FOUND);

        } catch (Exception e) {
            log.error("Exception occurred while handling GitHub OAuth callback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("GitHub OAuth failed");
        }
    }


}
