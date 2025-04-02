package com.Twitter.Jarvis.Controller;

import com.Twitter.Jarvis.Config.JwtProvider;
import com.Twitter.Jarvis.Exception.UserException;
import com.Twitter.Jarvis.Model.UserConfigurationModel;
import com.Twitter.Jarvis.Repository.UserRepository;
import com.Twitter.Jarvis.Response.AuthResponse;
import com.Twitter.Jarvis.Service.CustomUserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth/")
//@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);  //ek hi instance bne or alag bne (private static) koi accidental reassignment se bacne ke liye (final)

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private CustomUserDetailsServiceImpl customUserDetailsService;

    @PostMapping("signup")
    public ResponseEntity<AuthResponse> createUserHandler(@RequestBody UserConfigurationModel userConfigurationModel) throws Exception{

        try{
            String email = userConfigurationModel.getEmail();
            String password = userConfigurationModel.getPassword();
            String fullName = userConfigurationModel.getFullName();
            String birthDate = userConfigurationModel.getDateOfBirth();

            UserConfigurationModel isEmailExit = userRepository.findByEmail(email);
            if(isEmailExit != null){
                throw new UserException("Email is already used with another account");
            }

            UserConfigurationModel createdUser = new UserConfigurationModel();
            createdUser.setEmail(email);
            createdUser.setFullName(fullName);
            createdUser.setPassword(passwordEncoder.encode(password));
            createdUser.setDateOfBirth(birthDate);

            UserConfigurationModel savedUser = userRepository.save(createdUser);

            Authentication authentication = new UsernamePasswordAuthenticationToken(email,password);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = jwtProvider.generateToken(authentication);

            AuthResponse res = new AuthResponse(token, true);

            return new ResponseEntity<>(res, HttpStatus.CREATED);
        }
        catch (Exception e){
            logger.debug(e.getMessage());
//            System.out.println(e);
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @PostMapping("signin")
    public ResponseEntity<AuthResponse> userSignIn(@RequestBody UserConfigurationModel userConfigurationModel) throws Exception{
        try{
            String username = userConfigurationModel.getEmail();
            String password = userConfigurationModel.getPassword();

            Authentication authentication = authenticate(username,password);

            String token = jwtProvider.generateToken(authentication);

            AuthResponse res = new AuthResponse(token, true);

            return new ResponseEntity<>(res, HttpStatus.ACCEPTED);
        }
        catch (Exception e){
            System.out.println(e);
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @GetMapping("health")
    public String greeting(){
        return "good :)";
    }
    private Authentication authenticate(String username, String password) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        if(userDetails==null){
            throw new BadCredentialsException("Invalid username...");
        }
        if(!passwordEncoder.matches(password, userDetails.getPassword())){
            throw new BadCredentialsException("Invalid username or password");
        }
        return new UsernamePasswordAuthenticationToken(userDetails,null, userDetails.getAuthorities());

    }
}
