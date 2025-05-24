package com.monitoring.server.views.databases;

import org.springframework.beans.factory.annotation.Autowired;

import com.monitoring.server.data.entity.Database;
import com.monitoring.server.security.MenuSecurityHelper;
import com.monitoring.server.security.SecurityAnnotations.RequiresOperator;
import com.monitoring.server.service.interfaces.DatabaseService;
import com.monitoring.server.util.DatabaseConnectionTester;
import com.monitoring.server.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Vista para gestionar las bases de datos monitoreadas con seguridad basada en roles.
 * ADMIN: Acceso completo (CRUD)
 * OPERATOR: Solo lectura y prueba de conexiones
 */
@PageTitle("Bases de Datos Monitoreadas")
@Route(value = "databases", layout = MainLayout.class)
@RequiresOperator // Requires OPERATOR or higher (SYSADMIN)
public class DatabaseView extends VerticalLayout {

    private final DatabaseService databaseService;
    private final DatabaseConnectionTester connectionTester;
    private final MenuSecurityHelper securityHelper;
    private final Grid<Database> grid = new Grid<>(Database.class, false);
    private DatabaseForm form;

    /**
     * Constructor de la vista de bases de datos.
     */
    public DatabaseView(@Autowired DatabaseService databaseService, 
                       @Autowired DatabaseConnectionTester connectionTester,
                       @Autowired MenuSecurityHelper securityHelper) {
        this.databaseService = databaseService;
        this.connectionTester = connectionTester;
        this.securityHelper = securityHelper;
        
        addClassName("database-view");
        setSizeFull();
        
        add(createTitle());
        
        configureGrid();
        
        // Only show form for sysadmin
        if (securityHelper.canManageDatabases()) {
            configureForm();
            add(createToolbar());
            add(createContent());
            closeEditor();
        } else {
            // Read-only view for operators
            add(createReadOnlyToolbar());
            add(grid);
        }
        
        updateList();
    }

    /**
     * Crea el título de la vista.
     */
    private Component createTitle() {
        H2 title = new H2("Bases de Datos Monitoreadas");
        title.addClassNames(
            LumoUtility.FontSize.XLARGE,
            LumoUtility.Margin.Bottom.MEDIUM
        );
        
        // Add role indicator
        Span roleInfo = new Span();
        if (securityHelper.canManageDatabases()) {
            roleInfo.setText("Modo Administrador - Acceso completo");
            roleInfo.getStyle().set("color", "var(--lumo-success-color)");
        } else {
            roleInfo.setText("Modo Solo Lectura - Puede probar conexiones");
            roleInfo.getStyle().set("color", "var(--lumo-warning-color)");
        }
        
        VerticalLayout header = new VerticalLayout(title, roleInfo);
        header.setPadding(false);
        header.setSpacing(false);
        
        return header;
    }

    /**
     * Configura la tabla de bases de datos.
     */
    private void configureGrid() {
        grid.addClassNames("database-grid");
        grid.setSizeFull();
        
        grid.addColumn(Database::getName).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(Database::getType).setHeader("Tipo").setAutoWidth(true);
        grid.addColumn(Database::getHost).setHeader("Host").setAutoWidth(true);
        grid.addColumn(Database::getPort).setHeader("Puerto").setAutoWidth(true);
        
        grid.addColumn(new ComponentRenderer<>(database -> {
            Span status = new Span(database.getStatus());
            status.getElement().getThemeList().clear();
            
            if ("Activa".equals(database.getStatus())) {
                status.getElement().getThemeList().add("badge success");
            } else if ("En espera".equals(database.getStatus())) {
                status.getElement().getThemeList().add("badge");
            } else {
                status.getElement().getThemeList().add("badge error");
            }
            
            return status;
        })).setHeader("Estado").setAutoWidth(true);
        
        grid.addColumn(new ComponentRenderer<>(database -> {
            Span monitorEnabled = new Span(database.getMonitorEnabled() ? "Sí" : "No");
            return monitorEnabled;
        })).setHeader("Monitorear").setAutoWidth(true);
        
        grid.addColumn(new ComponentRenderer<>(this::createActionsColumn))
            .setHeader("Acciones").setAutoWidth(true);
        
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        
        // Only allow selection for sysadmin
        if (securityHelper.canManageDatabases()) {
            grid.asSingleSelect().addValueChangeListener(e -> {
                if (e.getValue() != null) {
                    editDatabase(e.getValue());
                }
            });
        }
    }

    /**
     * Creates action buttons based on user role
     */
    private Component createActionsColumn(Database database) {
        HorizontalLayout actions = new HorizontalLayout();
        
        // Test connection button - available to both roles
        Button testButton = new Button(new Icon(VaadinIcon.CONNECT));
        testButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        testButton.getElement().setAttribute("title", "Probar conexión");
        testButton.addClickListener(e -> testConnection(database));
        
        actions.add(testButton);
        
        // Edit and delete buttons only for sysadmin
        if (securityHelper.canManageDatabases()) {
            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.getElement().setAttribute("title", "Editar");
            editButton.addClickListener(e -> editDatabase(database));
            
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteButton.getElement().setAttribute("title", "Eliminar");
            deleteButton.addClickListener(e -> deleteDatabase(database));
            
            actions.add(editButton, deleteButton);
        }
        
        return actions;
    }

    /**
     * Configura el formulario de edición (solo para sysadmin).
     */
    private void configureForm() {
        form = new DatabaseForm(databaseService, connectionTester);
        form.setWidth("25em");
        
        form.addListener(DatabaseForm.SaveEvent.class, this::saveDatabase);
        form.addListener(DatabaseForm.CloseEvent.class, e -> closeEditor());
        form.addListener(DatabaseForm.DeleteEvent.class, e -> deleteDatabase(e.getDatabase()));
    }

    /**
     * Crea la barra de herramientas para sysadmin.
     */
    private Component createToolbar() {
        Button addButton = new Button("Agregar BD", new Icon(VaadinIcon.PLUS));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> addDatabase());
        
        HorizontalLayout toolbar = new HorizontalLayout(addButton);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.END);
        
        return toolbar;
    }

    /**
     * Crea la barra de herramientas para operadores (solo refrescar).
     */
    private Component createReadOnlyToolbar() {
        Button refreshButton = new Button("Actualizar", new Icon(VaadinIcon.REFRESH));
        refreshButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshButton.addClickListener(e -> updateList());
        
        HorizontalLayout toolbar = new HorizontalLayout(refreshButton);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.END);
        
        return toolbar;
    }

    /**
     * Crea el contenido principal (tabla y formulario) para sysadmin.
     */
    private Component createContent() {
        HorizontalLayout content = new HorizontalLayout(grid, form);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, form);
        content.setSizeFull();
        
        return content;
    }

    /**
     * Actualiza la lista de bases de datos.
     */
    private void updateList() {
        grid.setItems(databaseService.findAll());
    }

    /**
     * Cierra el editor de formulario.
     */
    private void closeEditor() {
        if (form != null) {
            form.setDatabase(null);
            form.setVisible(false);
            grid.asSingleSelect().clear();
        }
    }

    /**
     * Abre el editor para agregar una nueva base de datos (solo sysadmin).
     */
    private void addDatabase() {
        if (!securityHelper.canManageDatabases()) {
            showPermissionDeniedNotification();
            return;
        }
        
        grid.asSingleSelect().clear();
        Database newDatabase = new Database();
        newDatabase.setStatus("Inactiva");
        newDatabase.setMonitorEnabled(false);
        editDatabase(newDatabase);
    }

    /**
     * Abre el editor para editar una base de datos existente (solo sysadmin).
     */
    private void editDatabase(Database database) {
        if (!securityHelper.canManageDatabases()) {
            showPermissionDeniedNotification();
            return;
        }
        
        if (database == null) {
            closeEditor();
        } else {
            form.setDatabase(database);
            form.setVisible(true);
        }
    }

    /**
     * Guarda una base de datos (solo sysadmin).
     */
    private void saveDatabase(DatabaseForm.SaveEvent event) {
        if (!securityHelper.canManageDatabases()) {
            showPermissionDeniedNotification();
            return;
        }
        
        try {
            databaseService.save(event.getDatabase());
            updateList();
            closeEditor();
            
            Notification notification = Notification.show("Base de datos guardada");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification notification = Notification.show("Error al guardar: " + e.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Elimina una base de datos (solo sysadmin).
     */
    private void deleteDatabase(Database database) {
        if (!securityHelper.canManageDatabases()) {
            showPermissionDeniedNotification();
            return;
        }
        
        try {
            databaseService.deleteById(database.getId());
            updateList();
            closeEditor();
            
            Notification notification = Notification.show("Base de datos eliminada");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification notification = Notification.show("Error al eliminar: " + e.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Prueba la conexión a una base de datos (disponible para operadores y sysadmin).
     */
    private void testConnection(Database database) {
        try {
            boolean success = connectionTester.testConnection(database);
            
            if (success) {
                Notification notification = Notification.show("Conexión exitosa");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                
                // Update status only if user can manage databases
                if (securityHelper.canManageDatabases() && !"Activa".equals(database.getStatus())) {
                    databaseService.updateStatus(database.getId(), "Activa");
                    updateList();
                }
            } else {
                Notification notification = Notification.show("Conexión fallida");
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                
                // Update status only if user can manage databases
                if (securityHelper.canManageDatabases() && !"Inactiva".equals(database.getStatus())) {
                    databaseService.updateStatus(database.getId(), "Inactiva");
                    updateList();
                }
            }
        } catch (Exception e) {
            Notification notification = Notification.show("Error al probar conexión: " + e.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Muestra notificación de permisos insuficientes.
     */
    private void showPermissionDeniedNotification() {
        Notification notification = Notification.show(
            "No tienes permisos suficientes para realizar esta acción. Se requiere rol de Administrador del Sistema."
        );
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}