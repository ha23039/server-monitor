package com.monitoring.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.web.SecurityFilterChain;

import com.vaadin.flow.spring.security.VaadinWebSecurity;

@Configuration
@EnableWebSecurity
public class VaadinSecurityConfig extends VaadinWebSecurity {

    @Value("${auth0.clientId}")
    private String clientId;

    @Value("${auth0.clientSecret}")
    private String clientSecret;

    @Value("${auth0.domain}")
    private String domain;

    @Bean
    @Order(2) // Menor prioridad - se ejecuta después del API filter
    public SecurityFilterChain vaadinSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(request -> 
                // Aplica a todas las rutas EXCEPTO las que empiecen con /api/
                !request.getRequestURI().startsWith("/api/"))
            .authorizeHttpRequests(auth -> auth
                // Recursos estáticos de Vaadin
                .requestMatchers("/VAADIN/**", "/favicon.ico", "/robots.txt", 
                                "/manifest.webmanifest", "/sw.js", "/offline.html", 
                                "/icons/**", "/images/**", "/styles/**").permitAll()
                // Rutas de autenticación OAuth2
                .requestMatchers("/login/**", "/oauth2/**").permitAll()
                // Ruta de logout
                .requestMatchers("/logout").permitAll()
                // Actuator health check
                .requestMatchers("/actuator/health").permitAll()
                // Todas las demás rutas requieren autenticación
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(auth0ClientRegistration());
    }

    private ClientRegistration auth0ClientRegistration() {
        return ClientRegistration.withRegistrationId("auth0")
            .clientId(clientId)
            .clientSecret(clientSecret)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope("openid", "profile", "email")
            .authorizationUri("https://" + domain + "/authorize")
            .tokenUri("https://" + domain + "/oauth/token")
            .userInfoUri("https://" + domain + "/userinfo")
            .userNameAttributeName(IdTokenClaimNames.SUB)
            .jwkSetUri("https://" + domain + "/.well-known/jwks.json")
            .clientName("Auth0")
            .build();
    }
}