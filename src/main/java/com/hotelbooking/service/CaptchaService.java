package com.hotelbooking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CaptchaService {

    @Value("${google.recaptcha.secret}")
    private String recaptchaSecret;

    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    public boolean verifyCaptcha(String token) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> body = Map.of(
                "secret", recaptchaSecret,
                "response", token
        );

        Map response = restTemplate.postForObject(VERIFY_URL + "?secret={secret}&response={response}", 
                                                  null, Map.class, body);
        return response != null && Boolean.TRUE.equals(response.get("success"));
    }
}
