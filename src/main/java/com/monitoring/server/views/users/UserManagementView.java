package com.monitoring.server.views.users;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.monitoring.server.data.entity.User;
import com.monitoring.server.data.entity.User.UserRole;
import com.monitoring.server.security.SecurityAnnotations.RequiresSysAdmin;
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
 * Vista para gestión de usuarios - Solo accesible para SYSADMIN
 */
@Route(value = "users", layout = MainLayout.class)
@PageTitle("Gestión de Usuarios")
@RequiresSysAdmin
public class UserManagementView extends VerticalLayout {

    @Autowired
    private AuthService authService;

    private Grid<User> userGrid;

    public UserManagementView(@Autowired AuthService authService) {
        this.authService = authService;
        
        addClassName("user-management-view");
        setSizeFull();
        
        add(createTitle());
        add(createUserGrid());
        
        refreshUserGrid();
    }

    private Component createTitle() {
        H2 title = new H2("Gestión de Usuarios");
        title.addClassNames(
            LumoUtility.FontSize.XLARGE,
            LumoUtility.Margin.Bottom.MEDIUM
        );
        
        Span description = new Span("Administra los roles y permisos de los usuarios del sistema");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        VerticalLayout header = new VerticalLayout(title, description);
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
        
        switch (user.getRole()) {
            case SYSADMIN:
                badge.getElement().getThemeList().add("error");
                break;
            case OPERATOR:
                badge.getElement().getThemeList().add("contrast");
                break;
            case VIEWER:
                badge.getElement().getThemeList().add("success");
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
        roleComboBox.setItems(UserRole.values());
        roleComboBox.setItemLabelGenerator(this::getRoleDisplayName);
        roleComboBox.setValue(user.getRole());
        roleComboBox.setWidth("100%");

        content.add(userInfo, currentRole, roleComboBox);
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
                        // Reactivate user (you'll need to add this method to AuthService)
                        User updatedUser = authService.getCurrentUser().orElse(null);
                        if (updatedUser != null) {
                            user.setActive(true);
                            // You might want to add a reactivateUser method to AuthService
                        }
                    }
                    
                    refreshUserGrid();
                    
                    Notification notification = Notification.show(
                        "Usuario " + (user.isActive() ? "desactivado" : "activado") + " exitosamente"
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
        List<User> users = authService.getAllUsers();
        userGrid.setItems(users);
    }

    private String getRoleDisplayName(UserRole role) {
        switch (role) {
            case SYSADMIN:
                return "Administrador del Sistema";
            case OPERATOR:
                return "Operador";
            case VIEWER:
                return "Visualizador";
            default:
                return "Desconocido";
        }
    }
}