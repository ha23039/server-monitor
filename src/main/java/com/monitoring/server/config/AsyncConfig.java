package com.monitoring.server.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 🔧 Configuración consolidada para tareas asíncronas, programadas y exportación
 * Optimiza el rendimiento de CRON jobs, recolección de métricas y exportación
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
     * 📊 Executor para tareas de exportación (CSV/PDF/Excel/JSON)
     * ✅ MEJORADO: Configuración optimizada para exportaciones grandes
     * Separado del collector para no interferir con la recolección en tiempo real
     */
    @Bean(name = "exportExecutor") 
    public Executor exportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Configuración optimizada para exportaciones
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);         // Aumentado para mejor rendimiento
        executor.setQueueCapacity(100);     // Aumentado para manejar más tareas
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("ServerMonitor-Export-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        // Política de rechazo mejorada - Si el pool está lleno, ejecutar en el hilo que llama
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        return executor;
    }
    
    /**
     * 🧹 Executor para tareas de limpieza de archivos temporales de exportación
     * ✅ NUEVO: Dedicado a cleanup de exports para no saturar otros pools
     */
    @Bean(name = "cleanupExecutor")
    public Executor cleanupExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(10);
        executor.setKeepAliveSeconds(300);   // 5 minutos - tareas de limpieza son menos frecuentes
        
        executor.setThreadNamePrefix("ServerMonitor-Cleanup-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        
        // Para cleanup, usar DiscardPolicy - si está saturado, descartar tareas de limpieza
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.DiscardPolicy());
        
        executor.initialize();
        return executor;
    }
    
    /**
     * Executor para procesamiento de datos pesados (opcional)
     * Útil para análisis complejos y generación de reportes avanzados
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
    
    /**
     * 📧 Executor para notificaciones asíncronas (futuro)
     * ✅ NUEVO: Para envío de emails, webhooks, etc. sin bloquear exportaciones
     */
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(20);
        executor.setKeepAliveSeconds(120);
        
        executor.setThreadNamePrefix("ServerMonitor-Notification-");
        executor.setWaitForTasksToCompleteOnShutdown(false); // No bloquear shutdown por notificaciones
        executor.setAwaitTerminationSeconds(5);
        
        // Para notificaciones, usar DiscardOldestPolicy - mantener las más recientes
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy());
        
        executor.initialize();
        return executor;
    }
}