package com.monitoring.server.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * ⚙️ Configuración de threading para exportación asíncrona
 * Optimiza el rendimiento de generación de reportes
 */
@Configuration
@EnableAsync
public class AsyncExportConfig {

    /**
     * 🚀 Executor especializado para operaciones de exportación
     */
    @Bean(name = "exportExecutor")
    public Executor exportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Configuración del pool
        executor.setCorePoolSize(2);           // Threads base
        executor.setMaxPoolSize(5);            // Máximo de threads
        executor.setQueueCapacity(100);        // Cola de tareas
        executor.setKeepAliveSeconds(60);      // Tiempo de vida threads inactivos
        
        // Nomenclatura de threads
        executor.setThreadNamePrefix("ServerMonitor-Export-");
        
        // Política de rechazo: CallerRuns (ejecutar en hilo principal si está saturado)
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        // Inicializar el executor
        executor.initialize();
        
        return executor;
    }
    
    /**
     * 📊 Executor para procesamiento de datos pesados
     */
    @Bean(name = "dataProcessingExecutor")
    public Executor dataProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Configuración más robusta para procesamiento de datos
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(30);
        
        executor.setThreadNamePrefix("ServerMonitor-DataProcessing-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        
        return executor;
    }
    
    /**
     * 🎨 Executor para generación de gráficos y visualizaciones
     */
    @Bean(name = "chartGenerationExecutor")
    public Executor chartGenerationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Configuración optimizada para generación de gráficos
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(25);
        executor.setKeepAliveSeconds(45);
        
        executor.setThreadNamePrefix("ServerMonitor-Chart-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        
        return executor;
    }
}