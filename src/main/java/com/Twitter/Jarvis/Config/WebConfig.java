package com.Twitter.Jarvis.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${frontend.url.local}")
    private String localfrontendURL;

    @Value("${frontend.url.prod}")
    private String prodFrontendUrl;

    @Value("${frontend.url.dev}")
    private String devFrontendUrl;

    @Value("${spring.profiles.active}")
    private String environment;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(getFrontendURL()) // Replace with your frontend's URL
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    public String getFrontendURL() {
        String frontendUrl;
        if(environment.equals("prod")) {
            frontendUrl = prodFrontendUrl;
        }
        else if(environment.equals("dev")) {
            frontendUrl = devFrontendUrl;
        }
        else {
            frontendUrl = localfrontendURL;
        }

        return frontendUrl;
    }
}
