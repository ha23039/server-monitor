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
    private UserRole role = UserRole.VIEWER;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Enum para roles
    public enum UserRole {
        SYSADMIN("sysadmin"),
        OPERATOR("operator"), 
        VIEWER("viewer");
        
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
            return VIEWER; // Default role
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
    
    // Utility methods for role checking
    public boolean isSysAdmin() {
        return role == UserRole.SYSADMIN;
    }
    
    public boolean isOperator() {
        return role == UserRole.OPERATOR;
    }
    
    public boolean isViewer() {
        return role == UserRole.VIEWER;
    }
    
    public boolean hasWriteAccess() {
        return role == UserRole.SYSADMIN;
    }
    
    public boolean canViewMetrics() {
        return role == UserRole.SYSADMIN || role == UserRole.OPERATOR || role == UserRole.VIEWER;
    }
    
    public boolean canManageAlerts() {
        return role == UserRole.SYSADMIN || role == UserRole.OPERATOR;
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