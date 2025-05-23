package com.monitoring.server.data.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.monitoring.server.data.entity.User;
import com.monitoring.server.data.entity.User.UserRole;

/**
 * Repository for User entity operations
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by Auth0 subject (sub claim)
     */
    Optional<User> findByAuth0Subject(String auth0Subject);
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find all users by role
     */
    List<User> findByRole(UserRole role);
    
    /**
     * Find all active users
     */
    List<User> findByIsActiveTrue();
    
    /**
     * Find active users by role
     */
    List<User> findByRoleAndIsActiveTrue(UserRole role);
    
    /**
     * Count users by role
     */
    long countByRole(UserRole role);
    
    /**
     * Check if user exists by Auth0 subject
     */
    boolean existsByAuth0Subject(String auth0Subject);
    
    /**
     * Check if user exists by email
     */
    boolean existsByEmail(String email);
    
    /**
     * Find all system administrators
     */
    @Query("SELECT u FROM User u WHERE u.role = 'SYSADMIN' AND u.isActive = true")
    List<User> findActiveSysAdmins();
}