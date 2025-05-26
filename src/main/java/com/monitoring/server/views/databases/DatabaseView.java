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
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
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
 * Vista para gestionar las bases de datos monitoreadas con seguridad basada en roles.
 * ADMIN: Acceso completo (CRUD)
 * OPERATOR: Solo lectura y prueba de conexiones
 */
@PageTitle("Bases de Datos Monitoreadas")
@Route(value = "databases", layout = MainLayout.class)
@RequiresOperator // Requires OPERATOR or higher (ADMIN)
public class DatabaseView extends VerticalLayout {

    private final DatabaseService databaseService;
    private final DatabaseConnectionTester connectionTester;
    private final MenuSecurityHelper securityHelper;
    private final Grid<Database> grid = new Grid<>(Database.class, false);
    private DatabaseForm form;
    
    // Para tracking de cambios
    private Database originalDatabase;
    private boolean hasUnsavedChanges = false;

    /**
     * Constructor de la vista de bases de datos.
     */
    public DatabaseView(@Autowired DatabaseService databaseService, 
                       @Autowired DatabaseConnectionTester connectionTester,
                       @Autowired MenuSecurityHelper securityHelper) {
        this.databaseService = databaseService;
        this.connectionTester = connectionTester;
        this.securityHelper = securityHelper;
        
        try {
            System.out.println("🔧 CONSTRUCTOR DatabaseView - INICIO");
            
            addClassName("database-view");
            setSizeFull();
            
            add(createTitle());
            
            configureGrid();
            
            // Only show form for admin
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
            
            System.out.println("🔧 CONSTRUCTOR DatabaseView - ÉXITO");
        } catch (Exception e) {
            System.err.println("❌ ERROR EN DatabaseView: " + e.getMessage());
            e.printStackTrace();
            add(new H2("Error: " + e.getMessage()));
        }
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
        
        // Only allow selection for admin
        if (securityHelper.canManageDatabases()) {
            grid.asSingleSelect().addValueChangeListener(e -> {
                if (e.getValue() != null) {
                    checkUnsavedChangesBeforeEdit(e.getValue());
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
        testButton.addClickListener(e -> confirmTestConnection(database));
        
        actions.add(testButton);
        
        // Edit and delete buttons only for admin
        if (securityHelper.canManageDatabases()) {
            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.getElement().setAttribute("title", "Editar");
            editButton.addClickListener(e -> checkUnsavedChangesBeforeEdit(database));
            
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteButton.getElement().setAttribute("title", "Eliminar");
            deleteButton.addClickListener(e -> confirmDeleteDatabase(database));
            
            actions.add(editButton, deleteButton);
        }
        
        return actions;
    }

    /**
     * Configura el formulario de edición (solo para admin).
     */
    private void configureForm() {
        form = new DatabaseForm(databaseService, connectionTester);
        form.setWidth("25em");
        
        form.addListener(DatabaseForm.SaveEvent.class, this::confirmSaveDatabase);
        form.addListener(DatabaseForm.CloseEvent.class, e -> checkUnsavedChangesBeforeClose());
        form.addListener(DatabaseForm.DeleteEvent.class, e -> confirmDeleteDatabase(e.getDatabase()));
    }

    /**
     * Crea la barra de herramientas para admin.
     */
    private Component createToolbar() {
        Button addButton = new Button("Agregar BD", new Icon(VaadinIcon.PLUS));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> confirmAddDatabase());
        
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
     * Crea el contenido principal (tabla y formulario) para admin.
     */
    private Component createContent() {
        HorizontalLayout content = new HorizontalLayout(grid, form);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, form);
        content.setSizeFull();
        
        return content;
    }

    // ========== MÉTODOS DE CONFIRMACIÓN ==========

    /**
     * Confirma antes de agregar una nueva base de datos.
     */
    private void confirmAddDatabase() {
        if (!securityHelper.canManageDatabases()) {
            showPermissionDeniedNotification();
            return;
        }

        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Nueva Base de Datos");
        dialog.setText("¿Está seguro que desea agregar una nueva base de datos?");
        
        dialog.setCancelable(true);
        dialog.setCancelText("Cancelar");
        dialog.setConfirmText("Sí, agregar");
        dialog.setConfirmButtonTheme("primary");
        
        dialog.addConfirmListener(e -> addDatabase());
        dialog.open();
    }

    /**
     * Confirma antes de probar conexión.
     */
    private void confirmTestConnection(Database database) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Probar Conexión");
        
        Div content = new Div();
        content.add(new Paragraph("¿Desea probar la conexión a la base de datos?"));
        content.add(new Paragraph("Base de datos: " + database.getName() + " (" + database.getType() + ")"));
        content.add(new Paragraph("Host: " + database.getHost() + ":" + database.getPort()));
        dialog.add(content);
        
        dialog.setCancelable(true);
        dialog.setCancelText("Cancelar");
        dialog.setConfirmText("Probar Conexión");
        dialog.setConfirmButtonTheme("primary");
        
        dialog.addConfirmListener(e -> testConnection(database));
        dialog.open();
    }

    /**
     * Confirma antes de guardar una base de datos.
     */
    private void confirmSaveDatabase(DatabaseForm.SaveEvent event) {
        if (!securityHelper.canManageDatabases()) {
            showPermissionDeniedNotification();
            return;
        }

        Database databaseToSave = event.getDatabase();
        boolean isNew = databaseToSave.getId() == null;
        
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(isNew ? "Crear Base de Datos" : "Actualizar Base de Datos");
        
        Div content = new Div();
        if (isNew) {
            content.add(new Paragraph("¿Está seguro que desea crear esta nueva base de datos?"));
        } else {
            content.add(new Paragraph("¿Está seguro que desea guardar los cambios?"));
            if (hasChanges(databaseToSave)) {
                content.add(new H3("Cambios detectados:"));
                content.add(createChangesPreview(databaseToSave));
            }
        }
        
        content.add(new Paragraph("Nombre: " + databaseToSave.getName()));
        content.add(new Paragraph("Tipo: " + databaseToSave.getType()));
        content.add(new Paragraph("Host: " + databaseToSave.getHost() + ":" + databaseToSave.getPort()));
        dialog.add(content);
        
        dialog.setCancelable(true);
        dialog.setCancelText("Cancelar");
        dialog.setConfirmText(isNew ? "Crear" : "Guardar");
        dialog.setConfirmButtonTheme("primary");
        
        dialog.addConfirmListener(e -> saveDatabase(event));
        dialog.open();
    }

    /**
     * Confirma antes de eliminar una base de datos.
     */
    private void confirmDeleteDatabase(Database database) {
        if (!securityHelper.canManageDatabases()) {
            showPermissionDeniedNotification();
            return;
        }

        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("⚠️ Eliminar Base de Datos");
        
        Div content = new Div();
        content.add(new Paragraph("¿Está seguro que desea eliminar esta base de datos?"));
        content.add(new Paragraph("Esta acción NO se puede deshacer."));
        content.add(new H3("Base de datos a eliminar:"));
        content.add(new Paragraph("• Nombre: " + database.getName()));
        content.add(new Paragraph("• Tipo: " + database.getType()));
        content.add(new Paragraph("• Host: " + database.getHost()));
        content.add(new Paragraph("• Estado: " + database.getStatus()));
        dialog.add(content);
        
        dialog.setCancelable(true);
        dialog.setCancelText("Cancelar");
        dialog.setConfirmText("Sí, eliminar");
        dialog.setConfirmButtonTheme("error primary");
        
        dialog.addConfirmListener(e -> deleteDatabase(database));
        dialog.open();
    }

    /**
     * Verifica cambios no guardados antes de editar otra BD.
     */
    private void checkUnsavedChangesBeforeEdit(Database database) {
        if (hasUnsavedChanges && form != null && form.isVisible()) {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Cambios no guardados");
            dialog.setText("Hay cambios sin guardar en el formulario actual. ¿Desea continuar sin guardar?");
            
            dialog.setCancelable(true);
            dialog.setCancelText("Cancelar");
            dialog.setConfirmText("Continuar sin guardar");
            dialog.setConfirmButtonTheme("error");
            
            dialog.addConfirmListener(e -> {
                hasUnsavedChanges = false;
                editDatabase(database);
            });
            dialog.open();
        } else {
            editDatabase(database);
        }
    }

    /**
     * Verifica cambios no guardados antes de cerrar el formulario.
     */
    private void checkUnsavedChangesBeforeClose() {
        if (hasUnsavedChanges) {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Cambios no guardados");
            dialog.setText("Hay cambios sin guardar. ¿Desea continuar sin guardar?");
            
            dialog.setCancelable(true);
            dialog.setCancelText("Cancelar");
            dialog.setConfirmText("Cerrar sin guardar");
            dialog.setConfirmButtonTheme("error");
            
            dialog.addConfirmListener(e -> {
                hasUnsavedChanges = false;
                closeEditor();
            });
            dialog.open();
        } else {
            closeEditor();
        }
    }

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * Verifica si hay cambios en la base de datos.
     */
    private boolean hasChanges(Database current) {
        if (originalDatabase == null || current == null) return false;
        
        return !originalDatabase.getName().equals(current.getName()) ||
               !originalDatabase.getType().equals(current.getType()) ||
               !originalDatabase.getHost().equals(current.getHost()) ||
               !originalDatabase.getPort().equals(current.getPort()) ||
               !originalDatabase.getUsername().equals(current.getUsername()) ||
               !originalDatabase.getStatus().equals(current.getStatus()) ||
               !originalDatabase.getMonitorEnabled().equals(current.getMonitorEnabled());
    }

    /**
     * Crea una vista previa de los cambios.
     */
    private Component createChangesPreview(Database current) {
        VerticalLayout changes = new VerticalLayout();
        changes.setPadding(false);
        changes.setSpacing(false);
        
        if (originalDatabase != null) {
            if (!originalDatabase.getName().equals(current.getName())) {
                changes.add(new Span("• Nombre: " + originalDatabase.getName() + " → " + current.getName()));
            }
            if (!originalDatabase.getType().equals(current.getType())) {
                changes.add(new Span("• Tipo: " + originalDatabase.getType() + " → " + current.getType()));
            }
            if (!originalDatabase.getHost().equals(current.getHost())) {
                changes.add(new Span("• Host: " + originalDatabase.getHost() + " → " + current.getHost()));
            }
            if (!originalDatabase.getPort().equals(current.getPort())) {
                changes.add(new Span("• Puerto: " + originalDatabase.getPort() + " → " + current.getPort()));
            }
            if (!originalDatabase.getStatus().equals(current.getStatus())) {
                changes.add(new Span("• Estado: " + originalDatabase.getStatus() + " → " + current.getStatus()));
            }
        }
        
        return changes;
    }

    // ========== MÉTODOS ORIGINALES ACTUALIZADOS ==========

    /**
     * Actualiza la lista de bases de datos.
     */
    private void updateList() {
        try {
            grid.setItems(databaseService.findAll());
        } catch (Exception e) {
            System.err.println("❌ Error cargando bases de datos: " + e.getMessage());
            Notification.show("Error cargando datos")
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Cierra el editor de formulario.
     */
    private void closeEditor() {
        if (form != null) {
            form.setDatabase(null);
            form.setVisible(false);
            grid.asSingleSelect().clear();
            hasUnsavedChanges = false;
            originalDatabase = null;
        }
    }

    /**
     * Abre el editor para agregar una nueva base de datos (solo admin).
     */
    private void addDatabase() {
        grid.asSingleSelect().clear();
        Database newDatabase = new Database();
        newDatabase.setStatus("Inactiva");
        newDatabase.setMonitorEnabled(false);
        editDatabase(newDatabase);
    }

    /**
     * Abre el editor para editar una base de datos existente (solo admin).
     */
    private void editDatabase(Database database) {
        if (!securityHelper.canManageDatabases()) {
            showPermissionDeniedNotification();
            return;
        }
        
        if (database == null) {
            closeEditor();
        } else {
            // Guardar copia original para detectar cambios
            originalDatabase = database.getId() != null ? cloneDatabase(database) : null;
            hasUnsavedChanges = false;
            
            form.setDatabase(database);
            form.setVisible(true);
        }
    }

    /**
     * Crea una copia de la base de datos para detectar cambios.
     */
    private Database cloneDatabase(Database original) {
        Database clone = new Database();
        clone.setId(original.getId());
        clone.setName(original.getName());
        clone.setType(original.getType());
        clone.setHost(original.getHost());
        clone.setPort(original.getPort());
        clone.setUsername(original.getUsername());
        clone.setPassword(original.getPassword());
        clone.setStatus(original.getStatus());
        clone.setMonitorEnabled(original.getMonitorEnabled());
        return clone;
    }

    /**
     * Guarda una base de datos (solo admin).
     */
    private void saveDatabase(DatabaseForm.SaveEvent event) {
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
     * Elimina una base de datos (solo admin).
     */
    private void deleteDatabase(Database database) {
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
     * Prueba la conexión a una base de datos (disponible para operadores y admin).
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