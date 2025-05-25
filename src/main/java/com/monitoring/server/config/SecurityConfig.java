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
@EnableMethodSecurity(prePostEnabled = true) // IMPORTANTE: Habilitar @PreAuthorize
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
                // Rutas para operators o admin
                .requestMatchers("/api/operator/**").hasAnyAuthority("ROLE_admin", "ROLE_operator")
                // Rutas para usuarios autenticados (cualquier rol)
                .requestMatchers("/api/user/**").hasAnyAuthority("ROLE_admin", "ROLE_operator", "ROLE_viewer", "ROLE_user")
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
                .requestMatchers("/access-denied", "/error").permitAll()
                
                // Rutas de Vaadin - IMPORTANTES PARA QUE FUNCIONE
                .requestMatchers("/VAADIN/**", "/frontend/**", "/frontend-es6/**", "/frontend-es5/**").permitAll()
                .requestMatchers("/sw.js", "/manifest.webmanifest", "/offline.html").permitAll()
                .requestMatchers("/icons/**", "/images/**", "/styles/**", "/favicon.ico").permitAll()
                
                // Actuator health check
                .requestMatchers("/actuator/health").permitAll()
                
                // *** IMPORTANTE: NO definir restricciones aquí para las vistas ***
                // Las vistas usan @PreAuthorize en los métodos/clases, no URL-based security
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
            
            System.out.println("🔍 ROLES EXTRAÍDOS: " + roles);
            
            // *** SINCRONIZAR USUARIO CON NUESTRA BASE DE DATOS ***
            try {
                authService.createOrUpdateUser(oidcUser);
                System.out.println("✅ Usuario sincronizado: " + oidcUser.getEmail() + " con roles: " + roles);
            } catch (Exception e) {
                // Log error pero no interrumpir el login
                System.err.println("❌ Error sincronizando usuario: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Convertir roles a authorities con prefijo ROLE_
            List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> {
                    // Asegurar que el rol tenga el prefijo ROLE_
                    String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                    return new SimpleGrantedAuthority(roleWithPrefix);
                })
                .collect(Collectors.toList());

            System.out.println("✅ AUTHORITIES CREADAS: " + authorities);

            return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
        };
    }


    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            System.out.println("🎉 LOGIN EXITOSO para: " + authentication.getName());
            System.out.println("🔑 AUTHORITIES: " + authentication.getAuthorities());
            
            // Debug: Imprimir cada authority individualmente
            authentication.getAuthorities().forEach(auth -> 
                System.out.println("   🏷️  Authority: '" + auth.getAuthority() + "'")
            );
            
            // ✅ CAMBIO CRÍTICO: Redirigir a "/" en lugar de "/home"
            response.sendRedirect("/");
        };
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Extraer roles de Auth0 claims usando el namespace personalizado
            Collection<String> roles = extractRolesFromClaims(jwt.getClaims());
            
            return roles.stream()
                .map(role -> {
                    // Asegurar que el rol tenga el prefijo ROLE_
                    String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                    return new SimpleGrantedAuthority(roleWithPrefix);
                })
                .collect(Collectors.toList());
        });
        return converter;
    }

    @SuppressWarnings("unchecked")
    private Collection<String> extractRolesFromClaims(Map<String, Object> claims) {
        System.out.println("🔍 TODOS LOS CLAIMS: " + claims);
        
        // Auth0 custom claim namespace (debe coincidir con el Action)
        String rolesKey = "https://servermonitor.api/roles";
        
        Object rolesObj = claims.get(rolesKey);
        if (rolesObj instanceof Collection) {
            Collection<String> roles = (Collection<String>) rolesObj;
            System.out.println("✅ Roles extraídos del namespace custom: " + roles);
            return roles;
        } else if (rolesObj instanceof String) {
            List<String> roles = List.of((String) rolesObj);
            System.out.println("✅ Rol extraído como string: " + roles);
            return roles;
        }
        
        // Fallback: buscar en otros claims comunes
        Object authoritiesObj = claims.get("authorities");
        if (authoritiesObj instanceof Collection) {
            Collection<String> roles = (Collection<String>) authoritiesObj;
            System.out.println("✅ Roles extraídos de authorities: " + roles);
            return roles;
        }
        
        // Fallback: buscar roles directamente
        Object directRoles = claims.get("roles");
        if (directRoles instanceof Collection) {
            Collection<String> roles = (Collection<String>) directRoles;
            System.out.println("✅ Roles extraídos directamente: " + roles);
            return roles;
        }
        
        // Fallback final: buscar en app_metadata (Auth0 estándar)
        Object appMetadata = claims.get("https://servermonitor.api/app_metadata");
        if (appMetadata instanceof Map) {
            Map<String, Object> metadata = (Map<String, Object>) appMetadata;
            Object metadataRoles = metadata.get("roles");
            if (metadataRoles instanceof Collection) {
                Collection<String> roles = (Collection<String>) metadataRoles;
                System.out.println("✅ Roles extraídos de app_metadata: " + roles);
                return roles;
            }
        }
        
        // Si no encuentra roles, asignar rol viewer por defecto
        System.out.println("⚠️ No se encontraron roles, asignando 'viewer' por defecto");
        return List.of("viewer");
    }
}