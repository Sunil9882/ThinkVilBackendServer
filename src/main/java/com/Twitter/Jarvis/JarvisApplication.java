package com.Twitter.Jarvis;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JarvisApplication {

	public static void main(String[] args) {
		// Load .env variables
		Dotenv dotenv = Dotenv.load();
		System.setProperty("DB_URL", dotenv.get("DB_URL"));
		System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
		System.setProperty("FRONTEND_URL_LOCAL", dotenv.get("FRONTEND_URL_LOCAL"));
		System.setProperty("FRONTEND_URL_PROD", dotenv.get("FRONTEND_URL_PROD"));
		System.setProperty("FRONTEND_URL_DEV", dotenv.get("FRONTEND_URL_DEV"));
		SpringApplication.run(JarvisApplication.class, args);
	}

}
