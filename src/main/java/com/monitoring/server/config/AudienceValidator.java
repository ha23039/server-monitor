package com.monitoring.server.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Custom validator to check JWT audience claim
 */
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {
    
    private final String expectedAudience;

    public AudienceValidator(String expectedAudience) {
        this.expectedAudience = expectedAudience;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        OAuth2Error error = new OAuth2Error("invalid_audience", "The required audience is missing", null);

        if (jwt.getAudience().contains(expectedAudience)) {
            return OAuth2TokenValidatorResult.success();
        }

        return OAuth2TokenValidatorResult.failure(error);
    }
}