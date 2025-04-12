package com.Twitter.Jarvis.Controller;

import com.Twitter.Jarvis.Config.JwtProvider;
import com.Twitter.Jarvis.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("oauth2/authorization/")
public class CustomOAuth2SuccessController {


    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserService userService;

    @GetMapping("{provider}")
    public ResponseEntity<?> redirectToProvider(@PathVariable("provider") String provider, @RequestParam String code) {
        try {
            if(provider.equals("google")) {
                return userService.redirectToProvider(code);
            }
            else if(provider.equals("github")){
                return userService.redirectToProviderGithub(code);
            }
        }
        catch (Exception e) {

        }

        return null;
    }
}
