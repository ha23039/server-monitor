package com.monitoring.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.vaadin.flow.spring.security.VaadinWebSecurity;

/**
 * Vaadin-specific security configuration
 */
@Configuration
public class VaadinSecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Configure Vaadin-specific security first
        super.configure(http);
        
        // Set the login view
        setLoginView(http, "/login");
        
        // Configure OAuth2 for Vaadin - this handles the Auth0 login flow
        http.oauth2Login(oauth2 -> oauth2
            .loginPage("/login")
            .defaultSuccessUrl("/", true)
            .failureUrl("/login?error")
        );
        
        // Configure logout
        http.logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login")
            .deleteCookies("JSESSIONID")
            .invalidateHttpSession(true)
        );
    }
}