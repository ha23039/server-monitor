package com.monitoring.server.service.interfaces;

import java.util.List;
import java.util.Optional;

import com.monitoring.server.data.entity.Database;

/**
 * Interfaz que define las operaciones CRUD para la entidad Database.
 */
public interface DatabaseService {
    
    /**
     * Guarda o actualiza una base de datos.
     * @param database objeto Database a guardar
     * @return objeto Database guardado con su ID generado
     */
    Database save(Database database);
    
    /**
     * Obtiene todas las bases de datos.
     * @return Lista de todas las bases de datos
     */
    List<Database> findAll();
    
    /**
     * Obtiene una base de datos por su ID.
     * @param id ID de la base de datos
     * @return Base de datos con el ID especificado, o empty si no existe
     */
    Optional<Database> findById(Long id);
    
    /**
     * Elimina una base de datos por su ID.
     * @param id ID de la base de datos a eliminar
     */
    void deleteById(Long id);
    
    /**
     * Obtiene bases de datos por su estado de monitoreo.
     * @param monitorEnabled estado de monitoreo (true/false)
     * @return Lista de bases de datos con el estado de monitoreo especificado
     */
    List<Database> findByMonitorEnabled(Boolean monitorEnabled);
    
    /**
     * Obtiene bases de datos por su tipo.
     * @param type tipo de base de datos (MySQL, PostgreSQL, etc.)
     * @return Lista de bases de datos del tipo especificado
     */
    List<Database> findByType(String type);
    
    /**
     * Obtiene bases de datos por su estado.
     * @param status estado de la base de datos (Activa, Inactiva, etc.)
     * @return Lista de bases de datos con el estado especificado
     */
    List<Database> findByStatus(String status);
    
    /**
     * Prueba la conexión a una base de datos.
     * @param database objeto Database con la información de conexión
     * @return true si la conexión es exitosa, false de lo contrario
     */
    boolean testConnection(Database database);
    
    /**
     * Actualiza el estado de una base de datos.
     * @param id ID de la base de datos
     * @param status nuevo estado
     * @return Base de datos actualizada
     */
    Database updateStatus(Long id, String status);
    
    /**
     * Actualiza el estado de monitoreo de una base de datos.
     * @param id ID de la base de datos
     * @param monitorEnabled nuevo estado de monitoreo
     * @return Base de datos actualizada
     */
    Database updateMonitorEnabled(Long id, Boolean monitorEnabled);
}