package com.monitoring.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Auth0 configuration for JWT token validation
 */
@Configuration
public class Auth0Config {

    @Value("${auth0.audience}")
    private String audience;

    @Value("${auth0.domain}")
    private String domain;

    /**
     * JWT Decoder bean for Auth0 tokens
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        String issuerUri = String.format("https://%s/", domain);
        
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
            .withJwkSetUri(String.format("https://%s/.well-known/jwks.json", domain))
            .build();

        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withAudience);

        return jwtDecoder;
    }
}