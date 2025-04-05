package com.monitoring.server.views.databases;

import java.util.Arrays;
import java.util.List;

import com.monitoring.server.data.entity.Database;
import com.monitoring.server.service.interfaces.DatabaseService;
import com.monitoring.server.util.DatabaseConnectionTester;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;

/**
 * Formulario para crear y editar bases de datos a monitorear.
 * Permite la configuración de los parámetros de conexión y monitoreo.
 */
public class DatabaseForm extends FormLayout {
    
    private final DatabaseService databaseService;
    private final DatabaseConnectionTester connectionTester;
    
    private Database database;
    
    // Campos del formulario
    private TextField name = new TextField("Nombre");
    private ComboBox<String> type = new ComboBox<>("Tipo");
    private TextField host = new TextField("Host");
    private IntegerField port = new IntegerField("Puerto");
    private TextField username = new TextField("Usuario");
    private PasswordField password = new PasswordField("Contraseña");
    private ComboBox<String> status = new ComboBox<>("Estado");
    private Checkbox monitorEnabled = new Checkbox("Habilitar monitoreo");
    
    // Botones
    private Button save = new Button("Guardar");
    private Button delete = new Button("Eliminar");
    private Button close = new Button("Cancelar");
    private Button testConnection = new Button("Probar Conexión");
    
    // Binder para validación de datos
    private Binder<Database> binder = new BeanValidationBinder<>(Database.class);
    
    /**
     * Constructor del formulario.
     * @param databaseService servicio para operaciones CRUD de bases de datos
     * @param connectionTester probador de conexiones a bases de datos
     */
    public DatabaseForm(DatabaseService databaseService, DatabaseConnectionTester connectionTester) {
        this.databaseService = databaseService;
        this.connectionTester = connectionTester;
        
        addClassName("database-form");
        
        configureFields();
        configureBinder();
        
        add(
            name,
            type,
            host,
            port,
            username,
            password,
            status,
            monitorEnabled,
            createButtonsLayout()
        );
    }
    
    /**
     * Configura los campos del formulario.
     */
    private void configureFields() {
        // Configurar tipos de base de datos disponibles
        List<String> databaseTypes = Arrays.asList("MySQL", "PostgreSQL", "SQL Server", "Oracle", "MongoDB");
        type.setItems(databaseTypes);
        
        // Configurar estados disponibles
        List<String> statusOptions = Arrays.asList("Activa", "Inactiva", "En espera");
        status.setItems(statusOptions);
        
        // Hacer campos requeridos
        name.setRequired(true);
        type.setRequired(true);
        host.setRequired(true);
        port.setRequired(true);
        
        // Valores por defecto
        port.setValue(3306); // Puerto por defecto de MySQL
        status.setValue("Inactiva");
        monitorEnabled.setValue(false);
    }
    
    /**
     * Configura el binder para validación de datos.
     */
    private void configureBinder() {
        binder.bindInstanceFields(this);
        
        // Cuando cambia el tipo de base de datos, actualizar el puerto por defecto
        type.addValueChangeListener(event -> {
            String selectedType = event.getValue();
            if (selectedType != null) {
                switch (selectedType) {
                    case "MySQL":
                        port.setValue(3306);
                        break;
                    case "PostgreSQL":
                        port.setValue(5432);
                        break;
                    case "SQL Server":
                        port.setValue(1433);
                        break;
                    case "Oracle":
                        port.setValue(1521);
                        break;
                    case "MongoDB":
                        port.setValue(27017);
                        break;
                }
            }
        });
    }
    
    /**
     * Crea el layout de botones.
     * @return componente con los botones del formulario
     */
    private Component createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        testConnection.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        
        save.addClickShortcut(Key.ENTER);
        close.addClickShortcut(Key.ESCAPE);
        
        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, database)));
        close.addClickListener(event -> fireEvent(new CloseEvent(this)));
        testConnection.addClickListener(event -> testDatabaseConnection());
        
        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));
        
        return new HorizontalLayout(save, testConnection, delete, close);
    }
    
    /**
     * Prueba la conexión a la base de datos configurada.
     */
    private void testDatabaseConnection() {
        try {
            Database testDb = new Database();
            binder.writeBean(testDb);
            
            boolean success = connectionTester.testConnection(testDb);
            
            if (success) {
                Notification notification = Notification.show(
                        "Conexión exitosa a " + testDb.getName(),
                        5000,
                        Notification.Position.MIDDLE
                );
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                Notification notification = Notification.show(
                        "Error al conectar a " + testDb.getName() + ". Verifique los datos de conexión.",
                        5000,
                        Notification.Position.MIDDLE
                );
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } catch (ValidationException e) {
            Notification notification = Notification.show(
                    "Por favor complete todos los campos requeridos",
                    5000,
                    Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification notification = Notification.show(
                    "Error: " + e.getMessage(),
                    5000,
                    Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    /**
     * Valida y guarda los datos del formulario.
     */
    private void validateAndSave() {
        try {
            binder.writeBean(database);
            fireEvent(new SaveEvent(this, database));
        } catch (ValidationException e) {
            Notification notification = Notification.show(
                    "Error de validación: " + e.getMessage(),
                    5000,
                    Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    /**
     * Configura el formulario con los datos de la base de datos a editar.
     * @param database base de datos a editar
     */
    public void setDatabase(Database database) {
        this.database = database;
        binder.readBean(database);
    }
    
    // Eventos del formulario
    
    public static abstract class DatabaseFormEvent extends ComponentEvent<DatabaseForm> {
        private Database database;
        
        protected DatabaseFormEvent(DatabaseForm source, Database database) {
            super(source, false);
            this.database = database;
        }
        
        public Database getDatabase() {
            return database;
        }
    }
    
    public static class SaveEvent extends DatabaseFormEvent {
        SaveEvent(DatabaseForm source, Database database) {
            super(source, database);
        }
    }
    
    public static class DeleteEvent extends DatabaseFormEvent {
        DeleteEvent(DatabaseForm source, Database database) {
            super(source, database);
        }
    }
    
    public static class CloseEvent extends DatabaseFormEvent {
        CloseEvent(DatabaseForm source) {
            super(source, null);
        }
    }
    
    public <T extends ComponentEvent<?>> Registration addListener(
            Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}