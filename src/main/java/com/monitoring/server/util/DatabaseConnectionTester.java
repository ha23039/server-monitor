package com.monitoring.server.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.monitoring.server.data.entity.Database;

/**
 * Utilidad para probar conexiones a bases de datos.
 * Permite verificar si una base de datos está accesible.
 */
@Component
public class DatabaseConnectionTester {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionTester.class);
    private static final int CONNECTION_TIMEOUT = 5; // segundos
    
    // Mapeo de tipos de base de datos a drivers JDBC
    private static final Map<String, String> DRIVER_MAP = new HashMap<>();
    
    static {
        DRIVER_MAP.put("mysql", "com.mysql.cj.jdbc.Driver");
        DRIVER_MAP.put("postgresql", "org.postgresql.Driver");
        DRIVER_MAP.put("sql server", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        DRIVER_MAP.put("oracle", "oracle.jdbc.OracleDriver");
        DRIVER_MAP.put("mongodb", "mongodb.jdbc.MongoDriver");
    }

    /**
     * Prueba la conexión a una base de datos.
     * @param database objeto Database con la información de conexión
     * @return true si la conexión es exitosa, false si falla
     */
    public boolean testConnection(Database database) {
        Connection connection = null;
        
        try {
            // Obtener URL de conexión según el tipo de base de datos
            String url = buildConnectionUrl(database);
            String driverClass = getDriverClass(database.getType());
            
            // Cargar el driver JDBC
            log.debug("Cargando driver JDBC: {}", driverClass);
            Class.forName(driverClass);
            
            // Configurar timeout de conexión
            DriverManager.setLoginTimeout(CONNECTION_TIMEOUT);
            
            // Intentar establecer conexión
            log.info("Probando conexión a: {}", url);
            connection = DriverManager.getConnection(url, database.getUsername(), database.getPassword());
            
            return connection.isValid(CONNECTION_TIMEOUT);
        } catch (ClassNotFoundException e) {
            log.error("Driver JDBC no encontrado para {}: {}", database.getType(), e.getMessage());
            return false;
        } catch (SQLException e) {
            log.error("Error al conectar a base de datos {}: {}", database.getName(), e.getMessage());
            return false;
        } finally {
            // Cerrar la conexión si se estableció
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.error("Error al cerrar conexión: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Construye la URL de conexión según el tipo de base de datos.
     * @param database objeto Database con la información de conexión
     * @return URL de conexión JDBC
     */
    private String buildConnectionUrl(Database database) {
        String dbType = database.getType().toLowerCase();
        String host = database.getHost();
        int port = database.getPort();
        
        switch (dbType) {
            case "mysql":
                return "jdbc:mysql://" + host + ":" + port + "?useSSL=false&serverTimezone=UTC";
            case "postgresql":
                return "jdbc:postgresql://" + host + ":" + port + "/postgres";
            case "sql server":
            case "sqlserver":
                return "jdbc:sqlserver://" + host + ":" + port;
            case "oracle":
                return "jdbc:oracle:thin:@" + host + ":" + port + ":XE";
            case "mongodb":
                return "jdbc:mongodb://" + host + ":" + port;
            default:
                throw new IllegalArgumentException("Tipo de base de datos no soportado: " + dbType);
        }
    }

    /**
     * Obtiene la clase del driver JDBC según el tipo de base de datos.
     * @param dbType tipo de base de datos
     * @return nombre de la clase del driver
     */
    private String getDriverClass(String dbType) {
        String driverClass = DRIVER_MAP.get(dbType.toLowerCase());
        
        if (driverClass == null) {
            throw new IllegalArgumentException("Driver no encontrado para tipo de base de datos: " + dbType);
        }
        
        return driverClass;
    }
}