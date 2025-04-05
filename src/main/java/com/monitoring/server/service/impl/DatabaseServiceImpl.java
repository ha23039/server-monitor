package com.monitoring.server.service.impl;

import com.monitoring.server.data.entity.Database;
import com.monitoring.server.data.repository.DatabaseRepository;
import com.monitoring.server.service.interfaces.DatabaseService;
import com.monitoring.server.util.DatabaseConnectionTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementación de la interfaz DatabaseService.
 * Proporciona operaciones CRUD para la entidad Database.
 */
@Service
public class DatabaseServiceImpl implements DatabaseService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseServiceImpl.class);
    
    private final DatabaseRepository databaseRepository;
    private final DatabaseConnectionTester connectionTester;
    
    //@Autowired
    public DatabaseServiceImpl(DatabaseRepository databaseRepository, DatabaseConnectionTester connectionTester) {
        this.databaseRepository = databaseRepository;
        this.connectionTester = connectionTester;
    }

    @Override
    public Database save(Database database) {
        log.info("Guardando base de datos: {}", database.getName());
        return databaseRepository.save(database);
    }

    @Override
    public List<Database> findAll() {
        log.info("Obteniendo todas las bases de datos");
        return databaseRepository.findAll();
    }

    @Override
    public Optional<Database> findById(Long id) {
        log.info("Buscando base de datos con ID: {}", id);
        return databaseRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        log.info("Eliminando base de datos con ID: {}", id);
        databaseRepository.deleteById(id);
    }

    @Override
    public List<Database> findByMonitorEnabled(Boolean monitorEnabled) {
        log.info("Buscando bases de datos con monitoreo {}", monitorEnabled ? "habilitado" : "deshabilitado");
        return databaseRepository.findByMonitorEnabled(monitorEnabled);
    }

    @Override
    public List<Database> findByType(String type) {
        log.info("Buscando bases de datos de tipo: {}", type);
        return databaseRepository.findByType(type);
    }

    @Override
    public List<Database> findByStatus(String status) {
        log.info("Buscando bases de datos con estado: {}", status);
        return databaseRepository.findByStatus(status);
    }

    @Override
    public boolean testConnection(Database database) {
        log.info("Probando conexión a base de datos: {}", database.getName());
        boolean connectionSuccessful = connectionTester.testConnection(database);
        log.info("Resultado de prueba de conexión a {}: {}", 
                database.getName(), connectionSuccessful ? "exitosa" : "fallida");
        return connectionSuccessful;
    }

    @Override
    public Database updateStatus(Long id, String status) {
        log.info("Actualizando estado de base de datos ID {} a: {}", id, status);
        Optional<Database> databaseOpt = databaseRepository.findById(id);
        if (databaseOpt.isPresent()) {
            Database database = databaseOpt.get();
            database.setStatus(status);
            return databaseRepository.save(database);
        } else {
            log.error("No se encontró base de datos con ID: {}", id);
            throw new IllegalArgumentException("Base de datos no encontrada con ID: " + id);
        }
    }

    @Override
    public Database updateMonitorEnabled(Long id, Boolean monitorEnabled) {
        log.info("Actualizando estado de monitoreo de base de datos ID {} a: {}", 
                id, monitorEnabled ? "habilitado" : "deshabilitado");
        Optional<Database> databaseOpt = databaseRepository.findById(id);
        if (databaseOpt.isPresent()) {
            Database database = databaseOpt.get();
            database.setMonitorEnabled(monitorEnabled);
            return databaseRepository.save(database);
        } else {
            log.error("No se encontró base de datos con ID: {}", id);
            throw new IllegalArgumentException("Base de datos no encontrada con ID: " + id);
        }
    }
}