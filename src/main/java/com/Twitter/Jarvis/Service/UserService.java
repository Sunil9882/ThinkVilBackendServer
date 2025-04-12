package com.Twitter.Jarvis.Service;

import com.Twitter.Jarvis.Exception.UserException;
import com.Twitter.Jarvis.Model.UserConfigurationModel;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserService {

    ResponseEntity<?> redirectToProvider(String code) throws UserException;

    ResponseEntity<?> redirectToProviderGithub(String code) throws UserException;

}
