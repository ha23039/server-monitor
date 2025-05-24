package com.monitoring.server.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.monitoring.server.service.impl.AuthService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${auth0.domain}")
    private String auth0Domain;

    @Value("${auth0.audience}")
    private String auth0Audience;
    
    @Autowired
    private AuthService authService;

    @Bean
    @Order(1) // API Security - Mayor prioridad
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**") // Solo aplica a rutas que empiecen con /api/
            .csrf(csrf -> csrf.disable()) // Deshabilitamos CSRF para APIs
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Sin sesiones para APIs
            .oauth2ResourceServer(oauth2 -> 
                oauth2.jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter()))) // Convertir claims de Auth0
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas de la API
                .requestMatchers("/api/public/**", "/api/health/**").permitAll()
                // Rutas solo para administradores (usando roles de Auth0)
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_admin")
                // Rutas para usuarios autenticados
                .requestMatchers("/api/user/**").hasAnyAuthority("ROLE_user", "ROLE_admin")
                // Todas las demás rutas de API requieren autenticación
                .anyRequest().authenticated()
            );
        
        return http.build();
    }

    @Bean
    @Order(2) // Web Security - Segunda prioridad
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/**") // Aplica a todas las demás rutas
            .csrf(csrf -> csrf.disable()) // Vaadin maneja CSRF por su cuenta
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas
                .requestMatchers("/", "/login", "/oauth2/**", "/login/oauth2/**").permitAll()
                // Rutas de Vaadin
                .requestMatchers("/VAADIN/**", "/frontend/**", "/frontend-es6/**", "/frontend-es5/**").permitAll()
                // Actuator health check
                .requestMatchers("/actuator/health").permitAll()
                // Recursos estáticos
                .requestMatchers("/icons/**", "/images/**", "/styles/**", "/favicon.ico").permitAll()
                // Rutas protegidas - requieren autenticación
                .requestMatchers("/home/**", "/dashboard/**", "/databases/**", 
                                "/config/**", "/users/**", "/configurations/**").authenticated()
                // Todas las demás rutas requieren autenticación
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                    .oidcUserService(this.oidcUserService())
                )
                .successHandler(authenticationSuccessHandler())
                .failureUrl("/login?error")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
            );
        
        return http.build();
    }

    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        final OidcUserService delegate = new OidcUserService();

        return (userRequest) -> {
            OidcUser oidcUser = delegate.loadUser(userRequest);
            
            // Extraer roles de Auth0 usando el namespace personalizado
            Map<String, Object> claims = oidcUser.getClaims();
            Collection<String> roles = extractRolesFromClaims(claims);
            
            // *** SINCRONIZAR USUARIO CON NUESTRA BASE DE DATOS ***
            try {
                authService.createOrUpdateUser(oidcUser);
            } catch (Exception e) {
                // Log error pero no interrumpir el login
                System.err.println("Error sincronizando usuario: " + e.getMessage());
            }
            
            // Convertir roles a authorities
            List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

            return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
        };
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            // Redirigir después del login exitoso directamente al home
            response.sendRedirect("/home");
        };
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Extraer roles de Auth0 claims usando el namespace personalizado
            Collection<String> roles = extractRolesFromClaims(jwt.getClaims());
            
            return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        });
        return converter;
    }

    @SuppressWarnings("unchecked")
    private Collection<String> extractRolesFromClaims(Map<String, Object> claims) {
        // Auth0 custom claim namespace (debe coincidir con el Action)
        String rolesKey = "https://servermonitor.api/roles";
        
        Object rolesObj = claims.get(rolesKey);
        if (rolesObj instanceof Collection) {
            return (Collection<String>) rolesObj;
        } else if (rolesObj instanceof String) {
            return List.of((String) rolesObj);
        }
        
        // Fallback: buscar en otros claims comunes
        Object authoritiesObj = claims.get("authorities");
        if (authoritiesObj instanceof Collection) {
            return (Collection<String>) authoritiesObj;
        }
        
        // Fallback: buscar roles directamente
        Object directRoles = claims.get("roles");
        if (directRoles instanceof Collection) {
            return (Collection<String>) directRoles;
        }
        
        // Si no encuentra roles, asignar rol USER por defecto
        return List.of("user");
    }
}