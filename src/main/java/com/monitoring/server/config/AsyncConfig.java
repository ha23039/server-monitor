package com.monitoring.server.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuración para tareas asíncronas y programadas
 * Optimiza el rendimiento de los CRON jobs de recolección de métricas y exportación
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    /**
     * Executor para tareas de recolección de métricas
     * Configurado para manejar múltiples tareas concurrentes sin bloquear el hilo principal
     */
    @Bean(name = "metricCollectorExecutor")
    public Executor metricCollectorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Configuración del pool de hilos
        executor.setCorePoolSize(3);        // Hilos mínimos activos
        executor.setMaxPoolSize(8);         // Hilos máximos
        executor.setQueueCapacity(25);      // Cola de tareas pendientes
        executor.setKeepAliveSeconds(60);   // Tiempo de vida de hilos inactivos
        
        // Nombres descriptivos para debugging
        executor.setThreadNamePrefix("MetricCollector-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }
    
    /**
     * Executor para tareas de exportación (CSV/PDF)
     * Separado del collector para no interferir con la recolección en tiempo real
     */
    @Bean(name = "exportExecutor") 
    public Executor exportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);         // Aumentado para mejor rendimiento
        executor.setQueueCapacity(100);     // Aumentado para manejar más tareas
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("ServerMonitor-Export-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        // Política de rechazo mejorada
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        return executor;
    }
    
    /**
     * Executor para procesamiento de datos pesados (opcional)
     * Solo agregar si realmente lo necesitas
     */
    @Bean(name = "dataProcessingExecutor")
    public Executor dataProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(30);
        
        executor.setThreadNamePrefix("ServerMonitor-DataProcessing-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        return executor;
    }
}