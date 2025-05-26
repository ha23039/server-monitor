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
 * ✅ CORREGIDO: Sin constraint automático de Hibernate
 * ✅ Roles: admin, operator, viewer, user (minúsculas)
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
    
    // ✅ CORREGIDO: Sin constraint automático, solo STRING
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role = UserRole.VIEWER; // Default: VIEWER para máxima seguridad
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // ✅ ENUM CORREGIDO: Valores en minúsculas coincidiendo con Auth0
    public enum UserRole {
        ADMIN("admin"),         // ✅ Auth0: admin
        OPERATOR("operator"),   // ✅ Auth0: operator  
        VIEWER("viewer"),       // ✅ Auth0: viewer
        USER("user");           // ✅ Auth0: user
        
        private final String roleName;
        
        UserRole(String roleName) {
            this.roleName = roleName;
        }
        
        public String getRoleName() {
            return roleName;
        }
        
        // ✅ IMPORTANTE: toString() devuelve minúsculas para Hibernate
        @Override
        public String toString() {
            return roleName;
        }
        
        public static UserRole fromString(String roleName) {
            if (roleName == null) {
                return VIEWER; // Default seguro
            }
            
            // ✅ Coincidencia exacta primero
            for (UserRole role : UserRole.values()) {
                if (role.getRoleName().equalsIgnoreCase(roleName)) {
                    return role;
                }
            }
            
            // ✅ Mapeos especiales - SIN SYSADMIN
            String normalizedRole = roleName.toLowerCase().trim();
            switch (normalizedRole) {
                case "administrator":
                case "system-admin":
                    return ADMIN;
                case "ops":
                case "system-operator":
                    return OPERATOR;
                case "readonly":
                case "read-only":
                case "monitor":
                    return VIEWER;
                case "user":
                    return USER;
                // ✅ MIGRACIÓN: Si viene SYSADMIN, convertir a ADMIN
                case "sysadmin":
                    return ADMIN;
                default:
                    return VIEWER; // Default seguro
            }
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
        
        // Método para obtener descripción del rol
        public String getDescription() {
            switch (this) {
                case ADMIN:
                    return "Acceso completo al sistema: usuarios, configuración, todos los monitoreos";
                case OPERATOR:
                    return "Operaciones de monitoreo: bases de datos, alertas, configuraciones";
                case VIEWER:
                    return "Solo lectura: dashboards, métricas básicas";
                case USER:
                    return "Usuario básico: acceso limitado a funciones esenciales";
                default:
                    return "";
            }
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
        this.role = role != null ? role : UserRole.VIEWER; // Validación adicional
    }
    
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
        
        // Validación de seguridad
        if (this.role == null) {
            this.role = UserRole.VIEWER;
        }
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
        this.role = role != null ? role : UserRole.VIEWER; // Validación adicional
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
    
    // Métodos de permisos jerárquicos - JERARQUÍA CLARA
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
    
    // Métodos de permisos específicos del sistema
    public boolean canManageSystem() {
        return hasAdminPrivileges();
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
    
    public boolean canExportData() {
        return hasOperatorPrivileges();
    }
    
    public boolean canViewSystemLogs() {
        return hasOperatorPrivileges();
    }
    
    public boolean canCreateCustomDashboards() {
        return hasOperatorPrivileges();
    }
    
    // Método para obtener todos los permisos como lista (útil para debug)
    public String getPermissionsSummary() {
        StringBuilder permissions = new StringBuilder();
        permissions.append("Rol: ").append(role.getDisplayName()).append(" | ");
        permissions.append("Admin: ").append(hasAdminPrivileges()).append(" | ");
        permissions.append("Operator: ").append(hasOperatorPrivileges()).append(" | ");
        permissions.append("Viewer: ").append(hasViewerPrivileges());
        return permissions.toString();
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