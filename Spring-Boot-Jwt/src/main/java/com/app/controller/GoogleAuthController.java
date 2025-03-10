package com.app.controller;

import com.app.entity.User;
import com.app.repository.UserRepository;
import com.app.service.UserDetailsServiceImpl;
import com.app.utilis.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth/google")
public class GoogleAuthController {

    private static final Logger log = LoggerFactory.getLogger(GoogleAuthController.class);

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/callback")
    public ResponseEntity<?> handleGoogleCallback(@RequestParam String code) {
        log.info("Received OAuth2 callback with code: {}", code);

        try {
            String tokenEndpoint = "https://oauth2.googleapis.com/token";
            log.info("Sending request to token endpoint: {}", tokenEndpoint);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("redirect_uri", "https://developers.google.com/oauthplayground");
            params.add("grant_type", "authorization_code");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            log.info("Sending POST request to token endpoint with parameters: {}", params);

            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenEndpoint, request, Map.class);
            log.info("Token Response: {}", tokenResponse.getBody());

            if (tokenResponse.getStatusCode() != HttpStatus.OK) {
                log.error("Failed to obtain token. Response status: {}, Body: {}", tokenResponse.getStatusCode(), tokenResponse.getBody());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String idToken = (String) tokenResponse.getBody().get("id_token");
            log.info("Successfully received id_token: {}", idToken);

            String userInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            log.info("Fetching user info from URL: {}", userInfoUrl);

            ResponseEntity<Map> userInfoResponse = restTemplate.getForEntity(userInfoUrl, Map.class);
            if (userInfoResponse.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> userInfo = userInfoResponse.getBody();
                String email = (String) userInfo.get("email");
                log.info("Successfully fetched user info for email: {}", email);

                UserDetails userDetails = null;
                try {
                    userDetails = userDetailsService.loadUserByUsername(email);
                    log.info("User found in the system: {}", email);
                } catch (Exception e) {
                    log.warn("User not found in the system, creating new user with email: {}", email);
                    User user = new User();
                    user.setEmail(email);
                    user.setUserName(email);
                    user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    user.setRoles(Arrays.asList("USER"));
                    userRepository.save(user);
                    log.info("New user created and saved to the database: {}", email);
                }

                String jwtToken = jwtUtil.generateToken(email);
                log.info("JWT token successfully generated for user: {}", email);
                return ResponseEntity.ok(Collections.singletonMap("token", jwtToken));
            } else {
                log.error("Failed to retrieve user info. Response status: {}", userInfoResponse.getStatusCode());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            log.error("Exception occurred while handling Google callback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
