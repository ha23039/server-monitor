package com.monitoring.server.views.databases;

import com.monitoring.server.data.entity.Database;
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
import com.vaadin.flow.theme.lumo.LumoUtility;

import jakarta.annotation.security.PermitAll;

/**
 * Vista para gestionar las bases de datos monitoreadas.
 * Permite listar, agregar, editar y eliminar bases de datos.
 */
@PageTitle("Bases de Datos Monitoreadas")
@Route(value = "databases", layout = MainLayout.class)
@PermitAll
public class DatabaseView extends VerticalLayout {

    private final DatabaseService databaseService;
    private final DatabaseConnectionTester connectionTester; // Agregamos esta dependencia
    private final Grid<Database> grid = new Grid<>(Database.class, false);
    private DatabaseForm form;

    /**
     * Constructor de la vista de bases de datos.
     * @param databaseService servicio para gestionar bases de datos
     * @param connectionTester probador de conexiones a bases de datos
     */
    public DatabaseView(DatabaseService databaseService, DatabaseConnectionTester connectionTester) {
        this.databaseService = databaseService;
        this.connectionTester = connectionTester; // Inicializamos la dependencia
        
        addClassName("database-view");
        setSizeFull();
        
        add(createTitle());
        
        configureGrid();
        configureForm();
        
        add(createToolbar());
        add(createContent());
        
        updateList();
        closeEditor();
    }

    /**
     * Crea el título de la vista.
     * @return componente con el título
     */
    private Component createTitle() {
        H2 title = new H2("Bases de Datos Monitoreadas");
        title.addClassNames(
            LumoUtility.FontSize.XLARGE,
            LumoUtility.Margin.Bottom.MEDIUM
        );
        
        return title;
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
        
        grid.addColumn(new ComponentRenderer<>(database -> {
            HorizontalLayout actions = new HorizontalLayout();
            
            Button testButton = new Button(new Icon(VaadinIcon.CONNECT));
            testButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            testButton.getElement().setAttribute("title", "Probar conexión");
            testButton.addClickListener(e -> testConnection(database));
            
            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.getElement().setAttribute("title", "Editar");
            editButton.addClickListener(e -> editDatabase(database));
            
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteButton.getElement().setAttribute("title", "Eliminar");
            deleteButton.addClickListener(e -> deleteDatabase(database));
            
            actions.add(testButton, editButton, deleteButton);
            return actions;
        })).setHeader("Acciones").setAutoWidth(true);
        
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        
        grid.asSingleSelect().addValueChangeListener(e -> {
            if (e.getValue() != null) {
                editDatabase(e.getValue());
            }
        });
    }

    /**
     * Configura el formulario de edición.
     */
    private void configureForm() {
        form = new DatabaseForm(databaseService, connectionTester); // Pasamos las dependencias necesarias
        form.setWidth("25em");
        
        // Registramos los listeners para los eventos del formulario
        form.addListener(DatabaseForm.SaveEvent.class, this::saveDatabase);
        form.addListener(DatabaseForm.CloseEvent.class, e -> closeEditor());
        form.addListener(DatabaseForm.DeleteEvent.class, e -> deleteDatabase(e.getDatabase()));
    }

    /**
     * Crea la barra de herramientas.
     * @return componente con la barra de herramientas
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
     * Crea el contenido principal (tabla y formulario).
     * @return componente con el contenido
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
        form.setDatabase(null);
        form.setVisible(false);
        grid.asSingleSelect().clear();
    }

    /**
     * Abre el editor para agregar una nueva base de datos.
     */
    private void addDatabase() {
        grid.asSingleSelect().clear();
        Database newDatabase = new Database();
        newDatabase.setStatus("Inactiva");
        newDatabase.setMonitorEnabled(false);
        editDatabase(newDatabase);
    }

    /**
     * Abre el editor para editar una base de datos existente.
     * @param database base de datos a editar
     */
    private void editDatabase(Database database) {
        if (database == null) {
            closeEditor();
        } else {
            form.setDatabase(database);
            form.setVisible(true);
        }
    }

    /**
     * Guarda una base de datos (nueva o editada).
     * @param event evento con los datos a guardar
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
     * Elimina una base de datos.
     * @param database base de datos a eliminar
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
     * Prueba la conexión a una base de datos.
     * @param database base de datos a probar
     */
    private void testConnection(Database database) {
        try {
            boolean success = connectionTester.testConnection(database); // Usamos el connectionTester directamente
            
            if (success) {
                Notification notification = Notification.show("Conexión exitosa");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                
                // Actualizar estado a "Activa" si la conexión es exitosa
                if (!"Activa".equals(database.getStatus())) {
                    databaseService.updateStatus(database.getId(), "Activa");
                    updateList();
                }
            } else {
                Notification notification = Notification.show("Conexión fallida");
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                
                // Actualizar estado a "Inactiva" si la conexión falla
                if (!"Inactiva".equals(database.getStatus())) {
                    databaseService.updateStatus(database.getId(), "Inactiva");
                    updateList();
                }
            }
        } catch (Exception e) {
            Notification notification = Notification.show("Error al probar conexión: " + e.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}