package com.monitoring.server.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.monitoring.server.data.entity.User;
import com.monitoring.server.data.entity.User.UserRole;
import com.monitoring.server.data.repository.UserRepository;

/**
 * ⚠️ CONTROLADOR TEMPORAL - ELIMINAR DESPUÉS DE USAR
 * Solo para hacer admin a quique172016@gmail.com
 */
@RestController
@RequestMapping("/temp")
public class TempAdminController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Endpoint temporal para hacer admin a quique172016
     * Visitar: https://server-monitor-9zdf.onrender.com/temp/make-admin
     */
    @GetMapping("/make-admin")
    public String makeQuiqueAdmin() {
        try {
            // Buscar usuario por email
            Optional<User> userOpt = userRepository.findByEmail("quique172016@gmail.com");
            
            if (!userOpt.isPresent()) {
                return "❌ ERROR: Usuario quique172016@gmail.com no encontrado en la base de datos";
            }
            
            User quique = userOpt.get();
            
            // Mostrar estado antes
            String estadoAntes = "📋 ANTES: " + quique.getEmail() + " -> " + quique.getRole();
            
            // Cambiar rol a ADMIN directamente
            quique.setRole(UserRole.ADMIN);
            
            // Guardar usando el repository directamente (bypass AuthService validations)
            User updatedUser = userRepository.save(quique);
            
            // Mostrar estado después
            String estadoDespues = "✅ DESPUÉS: " + updatedUser.getEmail() + " -> " + updatedUser.getRole();
            
            return "<h1>🎉 OPERACIÓN COMPLETADA</h1>" +
                   "<p>" + estadoAntes + "</p>" +
                   "<p>" + estadoDespues + "</p>" +
                   "<p>🔗 <a href='/temp/verify'>Verificar resultado</a></p>" +
                   "<p>🏠 <a href='/'>Ir al Dashboard</a></p>";
            
        } catch (Exception e) {
            return "❌ ERROR: " + e.getMessage() + 
                   "<br><br>🔗 <a href='/temp/debug'>Ver debug info</a>";
        }
    }

    /**
     * Verificar el resultado del cambio
     * Visitar: https://server-monitor-9zdf.onrender.com/temp/verify
     */
    @GetMapping("/verify")
    public String verifyResult() {
        try {
            List<User> allUsers = userRepository.findAll();
            
            StringBuilder result = new StringBuilder();
            result.append("<h1>👥 ESTADO ACTUAL DE USUARIOS</h1>");
            result.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
            result.append("<tr style='background-color: #f0f0f0;'>");
            result.append("<th>Email</th><th>Rol</th><th>Nombre</th><th>Estado</th>");
            result.append("</tr>");
            
            for (User user : allUsers) {
                String emoji = "👤";
                String color = "#ffffff";
                
                switch (user.getRole()) {
                    case ADMIN:
                        emoji = "👑";
                        color = "#ffebee";
                        break;
                    case OPERATOR:
                        emoji = "🔧";
                        color = "#e3f2fd";
                        break;
                    case VIEWER:
                        emoji = "👁️";
                        color = "#e8f5e8";
                        break;
                    case USER:
                        emoji = "👤";
                        color = "#f5f5f5";
                        break;
                }
                
                result.append("<tr style='background-color: ").append(color).append(";'>");
                result.append("<td>").append(user.getEmail()).append("</td>");
                result.append("<td>").append(emoji).append(" ").append(user.getRole()).append("</td>");
                result.append("<td>").append(user.getName() != null ? user.getName() : "N/A").append("</td>");
                result.append("<td>").append(user.isActive() ? "✅ Activo" : "❌ Inactivo").append("</td>");
                result.append("</tr>");
            }
            
            result.append("</table>");
            result.append("<br><p>🏠 <a href='/'>Ir al Dashboard</a></p>");
            
            return result.toString();
            
        } catch (Exception e) {
            return "❌ ERROR verificando: " + e.getMessage();
        }
    }

    /**
     * Debug info
     * Visitar: https://server-monitor-9zdf.onrender.com/temp/debug
     */
    @GetMapping("/debug")
    public String debugInfo() {
        try {
            Optional<User> userOpt = userRepository.findByEmail("quique172016@gmail.com");
            
            if (!userOpt.isPresent()) {
                return "<h1>❌ USUARIO NO ENCONTRADO</h1>" +
                       "<p>quique172016@gmail.com no existe en la base de datos</p>" +
                       "<p>🔗 <a href='/temp/list-all'>Ver todos los usuarios</a></p>";
            }
            
            User user = userOpt.get();
            
            return "<h1>🐛 DEBUG INFO</h1>" +
                   "<p><strong>ID:</strong> " + user.getId() + "</p>" +
                   "<p><strong>Email:</strong> " + user.getEmail() + "</p>" +
                   "<p><strong>Auth0 Subject:</strong> " + user.getAuth0Subject() + "</p>" +
                   "<p><strong>Rol Actual:</strong> " + user.getRole() + "</p>" +
                   "<p><strong>Nombre:</strong> " + user.getName() + "</p>" +
                   "<p><strong>Activo:</strong> " + user.isActive() + "</p>" +
                   "<p><strong>Último Login:</strong> " + user.getLastLogin() + "</p>" +
                   "<p><strong>Creado:</strong> " + user.getCreatedAt() + "</p>" +
                   "<p><strong>Actualizado:</strong> " + user.getUpdatedAt() + "</p>" +
                   "<p>🔗 <a href='/temp/make-admin'>Intentar hacer admin</a></p>";
            
        } catch (Exception e) {
            return "❌ ERROR en debug: " + e.getMessage();
        }
    }

    /**
     * Listar todos los usuarios para debug
     */
    @GetMapping("/list-all")
    public String listAllUsers() {
        try {
            List<User> allUsers = userRepository.findAll();
            
            StringBuilder result = new StringBuilder();
            result.append("<h1>📋 TODOS LOS USUARIOS EN LA DB</h1>");
            result.append("<p>Total: ").append(allUsers.size()).append(" usuarios</p>");
            
            for (User user : allUsers) {
                result.append("<hr>");
                result.append("<p><strong>Email:</strong> ").append(user.getEmail()).append("</p>");
                result.append("<p><strong>Rol:</strong> ").append(user.getRole()).append("</p>");
                result.append("<p><strong>ID:</strong> ").append(user.getId()).append("</p>");
            }
            
            result.append("<br><p>🔗 <a href='/temp/make-admin'>Hacer admin a quique172016</a></p>");
            
            return result.toString();
            
        } catch (Exception e) {
            return "❌ ERROR listando usuarios: " + e.getMessage();
        }
    }
}