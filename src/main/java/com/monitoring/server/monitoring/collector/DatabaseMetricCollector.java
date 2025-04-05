package com.monitoring.server.monitoring.collector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.monitoring.server.data.entity.Database;
import com.monitoring.server.data.entity.SystemMetric;
import com.monitoring.server.service.interfaces.DatabaseService;
import com.monitoring.server.service.interfaces.MetricService;

/**
 * Recolector de métricas de bases de datos monitoreadas.
 * Obtienemos métricas como tiempo de respuesta, número de conexiones, etc.
 */
@Component
public class DatabaseMetricCollector {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMetricCollector.class);

    private final DatabaseService databaseService;
    private final MetricService metricService;
    private boolean isCollecting = false;

    @Autowired
    public DatabaseMetricCollector(DatabaseService databaseService, MetricService metricService) {
        this.databaseService = databaseService;
        this.metricService = metricService;
    }

    /**
     * Iniciamos la recolección de métricas de bases de datos.
     */
    public void startCollecting() {
        log.info("Iniciando recolección de métricas de bases de datos");
        isCollecting = true;
    }

    /**
     * Detiene la recolección de métricas de bases de datos.
     */
    public void stopCollecting() {
        log.info("Deteniendo recolección de métricas de bases de datos");
        isCollecting = false;
    }

    /**
     * Verifica si la recolección está activa.
     * @return true si la recolección está activa, false de lo contrario
     */
    public boolean isCollecting() {
        return isCollecting;
    }

    /**
     * Ejecuta la recolección de métricas de bases de datos periódicamente.
     * La anotación @Scheduled indica que se ejecutará cada 5 minutos.
     */
    @Scheduled(fixedRate = 300000)
    public void collectDatabaseMetrics() {
        if (!isCollecting) {
            return;
        }

        log.info("Recolectando métricas de bases de datos");
        
        // Obtener solo las bases de datos habilitadas para monitoreo
        List<Database> databases = databaseService.findByMonitorEnabled(true);
        Map<Long, List<SystemMetric>> allMetrics = new HashMap<>();
        
        for (Database db : databases) {
            List<SystemMetric> metrics = new ArrayList<>();
            LocalDateTime timestamp = LocalDateTime.now();
            
            try {
                // Verificar conexión y actualizar estado
                boolean connectionSuccessful = databaseService.testConnection(db);
                String status = connectionSuccessful ? "Activa" : "Inactiva";
                databaseService.updateStatus(db.getId(), status);
                
                if (connectionSuccessful) {
                    // Recolectamos métricas específicas según el tipo de base de datos
                    switch (db.getType().toLowerCase()) {
                        case "mysql":
                            metrics.addAll(collectMySQLMetrics(db, timestamp));
                            break;
                        case "postgresql":
                            metrics.addAll(collectPostgreSQLMetrics(db, timestamp));
                            break;
                        case "sql server":
                        case "sqlserver":
                            metrics.addAll(collectSQLServerMetrics(db, timestamp));
                            break;
                        default:
                            log.warn("Tipo de base de datos no soportado para métricas detalladas: {}", db.getType());
                            // Añadir al menos una métrica de tiempo de respuesta
                            metrics.add(new SystemMetric(
                                    "DB_" + db.getId() + "_ResponseTime",
                                    10.0, // Valor de ejemplo
                                    "ms",
                                    timestamp
                            ));
                    }
                    
                    // Guardar métricas en la base de datos
                    for (SystemMetric metric : metrics) {
                        metricService.save(metric);
                    }
                    
                    allMetrics.put(db.getId(), metrics);
                }
            } catch (Exception e) {
                log.error("Error al recolectar métricas para la base de datos {}: {}", db.getName(), e.getMessage());
                // Actualizar estado a "Inactiva" en caso de error
                databaseService.updateStatus(db.getId(), "Inactiva");
            }
        }
        
        log.info("Métricas de bases de datos recolectadas para {} bases de datos", allMetrics.size());
    }

    /**
     * Recolecta métricas específicas para MySQL.
     * @param db objeto Database con la información de conexión
     * @param timestamp momento de la recolección
     * @return lista de métricas recolectadas
     */
    private List<SystemMetric> collectMySQLMetrics(Database db, LocalDateTime timestamp) {
        List<SystemMetric> metrics = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            // Establecer conexión
            String url = "jdbc:mysql://" + db.getHost() + ":" + db.getPort() + "?useSSL=false&serverTimezone=UTC";
            conn = DriverManager.getConnection(url, db.getUsername(), db.getPassword());
            
            // Medir tiempo de respuesta
            long startTime = System.currentTimeMillis();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT 1");
            long endTime = System.currentTimeMillis();
            double responseTime = endTime - startTime;
            
            metrics.add(new SystemMetric(
                    "DB_" + db.getId() + "_ResponseTime",
                    responseTime,
                    "ms",
                    timestamp
            ));
            
            // Obtener número de conexiones
            rs = stmt.executeQuery("SHOW STATUS LIKE 'Threads_connected'");
            if (rs.next()) {
                int connections = rs.getInt(2);
                metrics.add(new SystemMetric(
                        "DB_" + db.getId() + "_Connections",
                        (double) connections,
                        "count",
                        timestamp
                ));
            }
            
            // Obtener espacio en disco
            rs = stmt.executeQuery("SELECT SUM(data_length + index_length) / 1024 / 1024 FROM information_schema.TABLES");
            if (rs.next()) {
                double diskSpace = rs.getDouble(1);
                metrics.add(new SystemMetric(
                        "DB_" + db.getId() + "_DiskSpace",
                        diskSpace,
                        "MB",
                        timestamp
                ));
            }
            
        } catch (Exception e) {
            log.error("Error al recolectar métricas MySQL para {}: {}", db.getName(), e.getMessage());
        } finally {
            // Cerrar recursos
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                log.error("Error al cerrar recursos de conexión", e);
            }
        }
        
        return metrics;
    }

    /**
     * Recolecta métricas específicas para PostgreSQL.
     * @param db objeto Database con la información de conexión
     * @param timestamp momento de la recolección
     * @return lista de métricas recolectadas
     */
    private List<SystemMetric> collectPostgreSQLMetrics(Database db, LocalDateTime timestamp) {
        List<SystemMetric> metrics = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            // Establecer conexión
            String url = "jdbc:postgresql://" + db.getHost() + ":" + db.getPort() + "/postgres";
            conn = DriverManager.getConnection(url, db.getUsername(), db.getPassword());
            
            // Medir tiempo de respuesta
            long startTime = System.currentTimeMillis();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT 1");
            long endTime = System.currentTimeMillis();
            double responseTime = endTime - startTime;
            
            metrics.add(new SystemMetric(
                    "DB_" + db.getId() + "_ResponseTime",
                    responseTime,
                    "ms",
                    timestamp
            ));
            
            // Obtener número de conexiones
            rs = stmt.executeQuery("SELECT count(*) FROM pg_stat_activity");
            if (rs.next()) {
                int connections = rs.getInt(1);
                metrics.add(new SystemMetric(
                        "DB_" + db.getId() + "_Connections",
                        (double) connections,
                        "count",
                        timestamp
                ));
            }
            
            // Obtener tamaño de la base de datos
            rs = stmt.executeQuery("SELECT pg_database_size('postgres') / 1024 / 1024");
            if (rs.next()) {
                double diskSpace = rs.getDouble(1);
                metrics.add(new SystemMetric(
                        "DB_" + db.getId() + "_DiskSpace",
                        diskSpace,
                        "MB",
                        timestamp
                ));
            }
            
        } catch (Exception e) {
            log.error("Error al recolectar métricas PostgreSQL para {}: {}", db.getName(), e.getMessage());
        } finally {
            // Cerrar recursos
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                log.error("Error al cerrar recursos de conexión", e);
            }
        }
        
        return metrics;
    }

    /**
     * Recolecta métricas específicas para SQL Server.
     * @param db objeto Database con la información de conexión
     * @param timestamp momento de la recolección
     * @return lista de métricas recolectadas
     */
    private List<SystemMetric> collectSQLServerMetrics(Database db, LocalDateTime timestamp) {
        List<SystemMetric> metrics = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            // Establecer conexión
            String url = "jdbc:sqlserver://" + db.getHost() + ":" + db.getPort();
            conn = DriverManager.getConnection(url, db.getUsername(), db.getPassword());
            
            // Medir tiempo de respuesta
            long startTime = System.currentTimeMillis();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT 1");
            long endTime = System.currentTimeMillis();
            double responseTime = endTime - startTime;
            
            metrics.add(new SystemMetric(
                    "DB_" + db.getId() + "_ResponseTime",
                    responseTime,
                    "ms",
                    timestamp
            ));
            
            // Obtener número de conexiones
            rs = stmt.executeQuery("SELECT COUNT(*) FROM sys.dm_exec_connections");
            if (rs.next()) {
                int connections = rs.getInt(1);
                metrics.add(new SystemMetric(
                        "DB_" + db.getId() + "_Connections",
                        (double) connections,
                        "count",
                        timestamp
                ));
            }
            
            // Obtener espacio en disco
            rs = stmt.executeQuery("SELECT SUM(size) * 8 / 1024 FROM sys.database_files");
            if (rs.next()) {
                double diskSpace = rs.getDouble(1);
                metrics.add(new SystemMetric(
                        "DB_" + db.getId() + "_DiskSpace",
                        diskSpace,
                        "MB",
                        timestamp
                ));
            }
            
        } catch (Exception e) {
            log.error("Error al recolectar métricas SQL Server para {}: {}", db.getName(), e.getMessage());
        } finally {
            // Cerrar recursos
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                log.error("Error al cerrar recursos de conexión", e);
            }
        }
        
        return metrics;
    }
}