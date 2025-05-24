package com.monitoring.server.views.users;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.monitoring.server.data.entity.User;
import com.monitoring.server.data.entity.User.UserRole;
import com.monitoring.server.security.SecurityAnnotations.RequiresAdmin;
import com.monitoring.server.service.impl.AuthService;
import com.monitoring.server.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Vista para gestión de usuarios - Solo accesible para ADMIN
 * ACTUALIZADA: Maneja todos los roles (ADMIN, OPERATOR, VIEWER, USER)
 */
@Route(value = "users", layout = MainLayout.class)
@PageTitle("Gestión de Usuarios")
@RequiresAdmin
public class UserManagementView extends VerticalLayout {

    @Autowired
    private AuthService authService;

    private Grid<User> userGrid;

    public UserManagementView(@Autowired AuthService authService) {
        this.authService = authService;
        
        // Verificar permisos
        if (!authService.canManageUsers()) {
            add(createAccessDenied());
            return;
        }
        
        addClassName("user-management-view");
        setSizeFull();
        
        add(createTitle());
        add(createUserGrid());
        
        refreshUserGrid();
        
        // Debug temporal
        authService.debugCurrentUser();
    }

    private Component createAccessDenied() {
        VerticalLayout layout = new VerticalLayout();
        layout.setAlignItems(Alignment.CENTER);
        layout.setJustifyContentMode(JustifyContentMode.CENTER);
        layout.setHeightFull();
        
        H2 title = new H2("🚫 Acceso Denegado");
        title.getStyle().set("color", "var(--lumo-error-color)");
        
        Span message = new Span("Solo los administradores pueden acceder a la gestión de usuarios.");
        message.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        Button backButton = new Button("🏠 Volver al Inicio", e -> {
            getUI().ifPresent(ui -> ui.navigate(""));
        });
        backButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        layout.add(title, message, backButton);
        return layout;
    }

    private Component createTitle() {
        H2 title = new H2("Gestión de Usuarios");
        title.addClassNames(
            LumoUtility.FontSize.XLARGE,
            LumoUtility.Margin.Bottom.MEDIUM
        );
        
        Span description = new Span("Administra los roles y permisos de los usuarios del sistema");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        // Mostrar información del usuario actual
        Span currentUserInfo = new Span("Usuario actual: " + authService.getCurrentUserName() + 
                                       " (" + authService.getCurrentUserRoleDisplay() + ")");
        currentUserInfo.getStyle().set("font-size", "var(--lumo-font-size-s)");
        currentUserInfo.getStyle().set("color", "var(--lumo-primary-color)");
        
        VerticalLayout header = new VerticalLayout(title, description, currentUserInfo);
        header.setPadding(false);
        header.setSpacing(false);
        
        return header;
    }

    private Component createUserGrid() {
        userGrid = new Grid<>(User.class, false);
        userGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        userGrid.setSizeFull();

        // Email column
        userGrid.addColumn(User::getEmail)
            .setHeader("Email")
            .setAutoWidth(true)
            .setSortable(true);

        // Name column
        userGrid.addColumn(user -> user.getName() != null ? user.getName() : "N/A")
            .setHeader("Nombre")
            .setAutoWidth(true)
            .setSortable(true);

        // Auth0 Subject column (para debug)
        userGrid.addColumn(user -> {
            String subject = user.getAuth0Subject();
            return subject != null ? subject.substring(0, Math.min(subject.length(), 20)) + "..." : "N/A";
        })
            .setHeader("Auth0 ID")
            .setAutoWidth(true);

        // Role column with badge
        userGrid.addColumn(new ComponentRenderer<>(this::createRoleBadge))
            .setHeader("Rol")
            .setAutoWidth(true);

        // Status column
        userGrid.addColumn(new ComponentRenderer<>(this::createStatusBadge))
            .setHeader("Estado")
            .setAutoWidth(true);

        // Last login column
        userGrid.addColumn(user -> user.getLastLogin() != null ? 
            user.getLastLogin().toString() : "Nunca")
            .setHeader("Último Acceso")
            .setAutoWidth(true);

        // Actions column
        userGrid.addColumn(new ComponentRenderer<>(this::createActionsLayout))
            .setHeader("Acciones")
            .setAutoWidth(true);

        return userGrid;
    }

    private Component createRoleBadge(User user) {
        Span badge = new Span(getRoleDisplayName(user.getRole()));
        badge.getElement().getThemeList().add("badge");
        
        // Colores para todos los roles
        switch (user.getRole()) {
            case ADMIN:
                badge.getElement().getThemeList().add("error"); // Rojo para admin
                break;
            case OPERATOR:
                badge.getElement().getThemeList().add("primary"); // Azul para operator
                break;
            case VIEWER:
                badge.getElement().getThemeList().add("success"); // Verde para viewer
                break;
            case USER:
                badge.getElement().getThemeList().add("contrast"); // Gris para user
                break;
            default:
                badge.getElement().getThemeList().add("contrast");
                break;
        }
        
        return badge;
    }

    private Component createStatusBadge(User user) {
        Span badge = new Span(user.isActive() ? "Activo" : "Inactivo");
        badge.getElement().getThemeList().add("badge");
        
        if (user.isActive()) {
            badge.getElement().getThemeList().add("success");
        } else {
            badge.getElement().getThemeList().add("error");
        }
        
        return badge;
    }

    private Component createActionsLayout(User user) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        // Change role button
        Button changeRoleButton = new Button(new Icon(VaadinIcon.USER_CHECK));
        changeRoleButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        changeRoleButton.getElement().setAttribute("title", "Cambiar rol");
        changeRoleButton.addClickListener(e -> showChangeRoleDialog(user));

        // Deactivate/Activate button
        Button toggleStatusButton = new Button(
            new Icon(user.isActive() ? VaadinIcon.USER_CLOCK : VaadinIcon.USER_CHECK)
        );
        toggleStatusButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        toggleStatusButton.getElement().setAttribute("title", 
            user.isActive() ? "Desactivar usuario" : "Activar usuario");
        toggleStatusButton.addClickListener(e -> toggleUserStatus(user));

        // Don't allow actions on current user
        User currentUser = authService.getCurrentUser().orElse(null);
        if (currentUser != null && currentUser.getId().equals(user.getId())) {
            changeRoleButton.setEnabled(false);
            toggleStatusButton.setEnabled(false);
            changeRoleButton.getElement().setAttribute("title", "No puedes modificar tu propio usuario");
        }

        actions.add(changeRoleButton, toggleStatusButton);
        return actions;
    }

    private void showChangeRoleDialog(User user) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Cambiar Rol de Usuario");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        
        Span userInfo = new Span("Usuario: " + user.getEmail());
        Span currentRole = new Span("Rol actual: " + getRoleDisplayName(user.getRole()));
        
        ComboBox<UserRole> roleComboBox = new ComboBox<>("Nuevo rol");
        roleComboBox.setItems(UserRole.values()); // Ahora incluye todos los roles
        roleComboBox.setItemLabelGenerator(this::getRoleDisplayName);
        roleComboBox.setValue(user.getRole());
        roleComboBox.setWidth("100%");
        
        // Descripción de roles
        Span roleDescription = new Span();
        roleDescription.getStyle().set("font-size", "var(--lumo-font-size-s)");
        roleDescription.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        roleComboBox.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                roleDescription.setText(getRoleDescription(e.getValue()));
            }
        });
        
        // Mostrar descripción inicial
        roleDescription.setText(getRoleDescription(user.getRole()));

        content.add(userInfo, currentRole, roleComboBox, roleDescription);
        dialog.add(content);

        dialog.setCancelable(true);
        dialog.setConfirmText("Cambiar Rol");
        dialog.setCancelText("Cancelar");

        dialog.addConfirmListener(event -> {
            UserRole newRole = roleComboBox.getValue();
            if (newRole != null && !newRole.equals(user.getRole())) {
                try {
                    authService.updateUserRole(user.getId(), newRole);
                    refreshUserGrid();
                    
                    Notification notification = Notification.show(
                        "Rol actualizado exitosamente para " + user.getEmail()
                    );
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } catch (Exception e) {
                    Notification notification = Notification.show(
                        "Error al actualizar rol: " + e.getMessage()
                    );
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            }
        });

        dialog.open();
    }

    private void toggleUserStatus(User user) {
        String action = user.isActive() ? "desactivar" : "activar";
        
        ConfirmDialog dialog = new ConfirmDialog(
            "Confirmar acción",
            "¿Estás seguro de que deseas " + action + " al usuario " + user.getEmail() + "?",
            "Confirmar",
            event -> {
                try {
                    if (user.isActive()) {
                        authService.deactivateUser(user.getId());
                    } else {
                        authService.activateUser(user.getId());
                    }
                    
                    refreshUserGrid();
                    
                    Notification notification = Notification.show(
                        "Usuario " + action + "do exitosamente"
                    );
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } catch (Exception e) {
                    Notification notification = Notification.show(
                        "Error al cambiar estado del usuario: " + e.getMessage()
                    );
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            },
            "Cancelar",
            event -> {}
        );
        
        dialog.open();
    }

    private void refreshUserGrid() {
        try {
            List<User> users = authService.getAllUsers();
            userGrid.setItems(users);
            
            // Debug
            System.out.println("💾 Usuarios cargados: " + users.size());
            users.forEach(user -> 
                System.out.println("  👤 " + user.getEmail() + " - " + user.getRole())
            );
        } catch (Exception e) {
            System.err.println("❌ Error cargando usuarios: " + e.getMessage());
            Notification.show("Error cargando usuarios: " + e.getMessage())
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private String getRoleDisplayName(UserRole role) {
        return role.getDisplayName(); // Usar el método del enum
    }
    
    private String getRoleDescription(UserRole role) {
        switch (role) {
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