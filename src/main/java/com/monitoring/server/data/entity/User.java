package com.monitoring.server.data.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Entidad User para almacenar información de usuarios autenticados con Auth0
 * Roles completos para coincidir con Auth0: admin, operator, viewer, user
 */
@Entity
@Table(name = "users")
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "auth0_subject", unique = true, nullable = false)
    private String auth0Subject; // Sub claim from Auth0 JWT
    
    @Column(name = "email", unique = true, nullable = false)
    private String email;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "nickname")
    private String nickname;
    
    @Column(name = "picture")
    private String picture;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.VIEWER; // Cambiado: USER -> VIEWER como default
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Enum COMPLETO para coincidir exactamente con Auth0
    public enum UserRole {
        ADMIN("admin"),         // Coincide con Auth0 "admin" 
        OPERATOR("operator"),   // Coincide con Auth0 "operator"
        VIEWER("viewer"),       // Coincide con Auth0 "viewer"
        USER("user");           // Coincide con Auth0 "user" (mapea a viewer)
        
        private final String roleName;
        
        UserRole(String roleName) {
            this.roleName = roleName;
        }
        
        public String getRoleName() {
            return roleName;
        }
        
        public static UserRole fromString(String roleName) {
            for (UserRole role : UserRole.values()) {
                if (role.getRoleName().equalsIgnoreCase(roleName)) {
                    return role;
                }
            }
            // Si recibe "user", mapear a VIEWER
            if ("user".equalsIgnoreCase(roleName)) {
                return VIEWER;
            }
            return VIEWER; // Default role
        }
        
        // Método para obtener el display name
        public String getDisplayName() {
            switch (this) {
                case ADMIN:
                    return "Administrador";
                case OPERATOR:
                    return "Operador";
                case VIEWER:
                    return "Visualizador";
                case USER:
                    return "Usuario";
                default:
                    return "Usuario";
            }
        }
        
        // Método para obtener el rol con prefijo ROLE_ para Spring Security
        public String getSpringRole() {
            return "ROLE_" + this.roleName;
        }
    }
    
    // Constructors
    public User() {
        this.createdAt = LocalDateTime.now();
    }
    
    public User(String auth0Subject, String email, String name, UserRole role) {
        this();
        this.auth0Subject = auth0Subject;
        this.email = email;
        this.name = name;
        this.role = role;
    }
    
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getAuth0Subject() {
        return auth0Subject;
    }
    
    public void setAuth0Subject(String auth0Subject) {
        this.auth0Subject = auth0Subject;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getPicture() {
        return picture;
    }
    
    public void setPicture(String picture) {
        this.picture = picture;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Utility methods para verificación de roles - COMPLETOS
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
    
    public boolean isOperator() {
        return role == UserRole.OPERATOR;
    }
    
    public boolean isViewer() {
        return role == UserRole.VIEWER || role == UserRole.USER;
    }
    
    public boolean isUser() {
        return role == UserRole.USER;
    }
    
    // Métodos de permisos jerárquicos
    public boolean hasAdminPrivileges() {
        return role == UserRole.ADMIN;
    }
    
    public boolean hasOperatorPrivileges() {
        return role == UserRole.ADMIN || role == UserRole.OPERATOR;
    }
    
    public boolean hasViewerPrivileges() {
        return role == UserRole.ADMIN || role == UserRole.OPERATOR || 
               role == UserRole.VIEWER || role == UserRole.USER;
    }
    
    // Métodos de permisos específicos
    public boolean canManageSystem() {
        return role == UserRole.ADMIN;
    }
    
    public boolean canViewMetrics() {
        return hasViewerPrivileges();
    }
    
    public boolean canConfigureAlerts() {
        return hasOperatorPrivileges();
    }
    
    public boolean canManageDatabases() {
        return hasOperatorPrivileges();
    }
    
    public boolean canManageUsers() {
        return hasAdminPrivileges();
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", auth0Subject='" + auth0Subject + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", role=" + role +
                ", isActive=" + isActive +
                ", lastLogin=" + lastLogin +
                ", createdAt=" + createdAt +
                '}';
    }
}