package com.monitoring.server.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuración para tareas asíncronas y programadas
 * Optimiza el rendimiento de los CRON jobs de recolección de métricas
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
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("Export-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        return executor;
    }
}