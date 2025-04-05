package com.monitoring.server.data.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.monitoring.server.data.entity.Alert;

/**
 * Repositorio para acceder y manipular datos de la entidad Alert.
 * Proporciona métodos para realizar operaciones CRUD.
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    
    /**
     * Encuentra alertas por nombre de componente.
     * @param componentName nombre del componente (CPU, RAM, Disco, etc.)
     * @return Lista de alertas para el componente especificado
     */
    List<Alert> findByComponentName(String componentName);
    
    /**
     * Encuentra alertas por estado.
     * @param status estado de la alerta (Nueva, Leída, Resuelta)
     * @return Lista de alertas con el estado especificado
     */
    List<Alert> findByStatus(String status);
    
    /**
     * Encuentra alertas generadas en un rango de tiempo.
     * @param start inicio del rango temporal
     * @param end fin del rango temporal
     * @return Lista de alertas generadas en el rango especificado
     */
    List<Alert> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Cuenta las alertas por estado.
     * @param status estado de la alerta (Nueva, Leída, Resuelta)
     * @return Número de alertas con el estado especificado
     */
    long countByStatus(String status);
}