package com.monitoring.server.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.monitoring.server.data.entity.Database;

/**
 * Repositorio para acceder y manipular datos de la entidad Database.
 * Proporciona métodos para realizar operaciones CRUD.
 */
@Repository
public interface DatabaseRepository extends JpaRepository<Database, Long> {
    
    /**
     * Encuentra bases de datos por su estado de monitoreo.
     * @param monitorEnabled estado de monitoreo (true/false)
     * @return Lista de bases de datos con el estado de monitoreo especificado
     */
    List<Database> findByMonitorEnabled(Boolean monitorEnabled);
    
    /**
     * Encuentra bases de datos por su tipo.
     * @param type tipo de base de datos (MySQL, PostgreSQL, etc.)
     * @return Lista de bases de datos del tipo especificado
     */
    List<Database> findByType(String type);
    
    /**
     * Encuentra bases de datos por su estado.
     * @param status estado de la base de datos (Activa, Inactiva, etc.)
     * @return Lista de bases de datos con el estado especificado
     */
    List<Database> findByStatus(String status);
}