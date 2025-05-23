package com.monitoring.server.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitoring.server.data.entity.User;
import com.monitoring.server.service.impl.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Controller for handling Auth0 authentication flow
 */
@RestController
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Value("${auth0.domain}")
    private String domain;

    @Value("${auth0.clientId}")
    private String clientId;

    @Value("${auth0.clientSecret}")
    private String clientSecret;

    @Value("${auth0.audience}")
    private String audience;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtDecoder jwtDecoder;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Handle Auth0 callback after authentication
     */
    @GetMapping("/callback")
    public void handleCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            HttpServletResponse response,
            HttpSession session) throws IOException {
        
        try {
            // Exchange authorization code for tokens
            String tokenResponse = exchangeCodeForTokens(code);
            Map<String, Object> tokens = objectMapper.readValue(tokenResponse, new TypeReference<Map<String, Object>>() {});
            
            String accessToken = (String) tokens.get("access_token");
            String idToken = (String) tokens.get("id_token");
            
            if (idToken != null) {
                // Decode and validate JWT
                Jwt jwt = jwtDecoder.decode(idToken);
                
                // Create or update user in database
                User user = authService.createOrUpdateUser(jwt);
                
                // Store tokens in session
                session.setAttribute("access_token", accessToken);
                session.setAttribute("id_token", idToken);
                session.setAttribute("user_id", user.getId());
                
                logger.info("User {} logged in successfully", user.getEmail());
                
                // Redirect to main application
                response.sendRedirect("/");
            } else {
                logger.error("No ID token received from Auth0");
                response.sendRedirect("/login?error=no_token");
            }
            
        } catch (Exception e) {
            logger.error("Error during Auth0 callback processing", e);
            response.sendRedirect("/login?error=callback_failed");
        }
    }

    /**
     * Logout endpoint
     */
    @GetMapping("/logout")
    public void logout(HttpServletResponse response, HttpSession session) throws IOException {
        // Clear session
        session.invalidate();
        
        // Build Auth0 logout URL
        String logoutUrl = String.format(
            "https://%s/v2/logout?client_id=%s&returnTo=%s",
            domain,
            clientId,
            URLEncoder.encode(getBaseUrl() + "/login", StandardCharsets.UTF_8)
        );
        
        response.sendRedirect(logoutUrl);
    }

    /**
     * Get current user info
     */
    @GetMapping("/api/user/me")
    public ResponseEntity<User> getCurrentUser() {
        return authService.getCurrentUser()
            .map(user -> ResponseEntity.ok(user))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Exchange authorization code for tokens
     */
    private String exchangeCodeForTokens(String code) throws IOException, InterruptedException {
        String tokenEndpoint = String.format("https://%s/oauth/token", domain);
        
        String requestBody = String.format(
            "grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s",
            clientId,
            clientSecret,
            code,
            URLEncoder.encode(getBaseUrl() + "/callback", StandardCharsets.UTF_8)
        );

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(tokenEndpoint))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to exchange code for tokens: " + response.body());
        }
        
        return response.body();
    }

    private String getBaseUrl() {
        // In production, this should be configured via environment variable
        return System.getProperty("app.baseUrl", "http://localhost:8080");
    }
}