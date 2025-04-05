package com.monitoring.server.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Clase utilitaria para ejecutar comandos del sistema operativo
 * Está diseñada para funcionar en Windows y Linux
 */
@Component
public class SystemCommandExecutor {

    private static final Logger logger = LoggerFactory.getLogger(SystemCommandExecutor.class);
    
    // Tiempo máximo de espera para la ejecución de comandos (en segundos)
    private static final long DEFAULT_TIMEOUT = 10;
    
    /**
     * Ejecuta un comando en el sistema operativo
     * @param command Comando a ejecutar
     * @return Salida del comando
     * @throws Exception Si ocurre un error durante la ejecución
     */
    public String executeCommand(String command) throws Exception {
        return executeCommand(command, DEFAULT_TIMEOUT);
    }
    
    /**
     * Ejecuta un comando en el sistema operativo con tiempo de espera personalizado
     * @param command Comando a ejecutar
     * @param timeoutSeconds Tiempo máximo de espera en segundos
     * @return Salida del comando
     * @throws Exception Si ocurre un error durante la ejecución
     */
    public String executeCommand(String command, long timeoutSeconds) throws Exception {
        logger.debug("Ejecutando comando: {}", command);
        
        Process process = null;
        StringBuilder output = new StringBuilder();
        
        try {
            // Determinar el shell adecuado según el sistema operativo
            ProcessBuilder processBuilder;
            String osName = System.getProperty("os.name").toLowerCase();
            
            if (osName.contains("win")) {
                // Windows
                processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
            } else {
                // Unix/Linux/Mac
                processBuilder = new ProcessBuilder("/bin/sh", "-c", command);
            }
            
            // Redireccionar error a salida estándar
            processBuilder.redirectErrorStream(true);
            
            // Iniciar el proceso
            process = processBuilder.start();
            
            // Leer la salida
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            // Esperar a que termine con timeout
            boolean completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            
            if (!completed) {
                logger.warn("Comando excedió el tiempo de espera: {}", command);
                process.destroyForcibly();
                throw new Exception("Timeout ejecutando comando: " + command);
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                logger.warn("Comando terminó con código de salida no cero: {} (Salida: {})", exitCode, output);
                throw new Exception("Comando terminó con código de salida: " + exitCode);
            }
            
            return output.toString();
            
        } catch (IOException e) {
            logger.error("Error de E/S ejecutando comando: {}", command, e);
            throw new Exception("Error ejecutando comando: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Comando interrumpido: {}", command, e);
            throw new Exception("Comando interrumpido: " + e.getMessage());
        } finally {
            // Asegurar que el proceso se termine
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }
    
    /**
     * Método específico para obtener los procesos más pesados del sistema
     * @param limit Número máximo de procesos a obtener
     * @return Lista de procesos con su información
     */
    public String getHeavyProcesses(int limit) throws Exception {
        String osName = System.getProperty("os.name").toLowerCase();
        String command;
        
        if (osName.contains("win")) {
            // Windows: usar tasklist y sort para obtener los procesos más pesados
            command = "tasklist /FO CSV /NH | sort /R /+58";
        } else {
            // Linux: usar ps para obtener los procesos más pesados por CPU
            command = "ps aux --sort=-%cpu | head -n " + (limit + 1);
        }
        
        return executeCommand(command);
    }
    
    /**
     * Verifica si un comando está disponible en el sistema
     * @param command Comando a verificar
     * @return true si el comando está disponible
     */
    public boolean isCommandAvailable(String command) {
        try {
            String checkCommand;
            String osName = System.getProperty("os.name").toLowerCase();
            
            if (osName.contains("win")) {
                // Windows
                checkCommand = "where " + command;
            } else {
                // Unix/Linux/Mac
                checkCommand = "which " + command;
            }
            
            executeCommand(checkCommand);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}