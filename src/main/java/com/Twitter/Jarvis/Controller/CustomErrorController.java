package com.Twitter.Jarvis.Controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Map<String, Object> errorResponse = new HashMap<>();
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            errorResponse.put("status", statusCode);

            errorResponse.put("message", switch (statusCode) {
                case 401 -> "Unauthorized access!";
                case 403 -> "Forbidden!";
                case 404 -> "Page not found!";
                case 500 -> "Internal server error!";
                default -> "An unexpected error occurred.";
            });

            return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(statusCode));
        }

        errorResponse.put("message", "Unknown error");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
