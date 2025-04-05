package com.monitoring.server.monitoring.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.monitoring.server.data.entity.ProcessInfo;
import com.monitoring.server.util.SystemCommandExecutor;

/**
 * Recolector de información de procesos del sistema
 * Compatible con Windows y Linux
 */
@Component
public class ProcessInfoCollector {

    private static final Logger logger = LoggerFactory.getLogger(ProcessInfoCollector.class);
    
    @Autowired
    private SystemCommandExecutor commandExecutor;
    
    private String osName = System.getProperty("os.name").toLowerCase();
    
    /**
     * Recolecta información de procesos del sistema
     * @return Lista de procesos con su información
     */
    public List<ProcessInfo> collectProcesses() {
        try {
            if (isWindows()) {
                return collectWindowsProcesses();
            } else {
                return collectLinuxProcesses();
            }
        } catch (Exception e) {
            logger.error("Error al recolectar procesos", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Recolecta información de procesos en Windows
     */
    private List<ProcessInfo> collectWindowsProcesses() throws Exception {
        List<ProcessInfo> processes = new ArrayList<>();
        
        String command = "wmic process get ProcessId,Name,WorkingSetSize,UserModeTime /format:csv";
        String output = commandExecutor.executeCommand(command);
        
        String[] lines = output.split("\n");
        // Saltar la primera línea (encabezados)
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            
            String[] parts = line.split(",");
            if (parts.length < 4) continue;
            
            try {
                String processId = parts[1].trim();
                String processName = parts[2].trim();
                long memoryInBytes = Long.parseLong(parts[3].trim());
                long cpuTime = Long.parseLong(parts[4].trim());
                
                // Calcular porcentajes aproximados
                double memoryUsage = (memoryInBytes / (double) (1024 * 1024 * 1024)) * 100; // Como % de 1 GB
                double cpuUsage = (cpuTime / 10000000.0) * 10; // Aproximación basada en ticks
                
                ProcessInfo process = new ProcessInfo(processId, processName, cpuUsage, memoryUsage);
                process.setUsername("N/A"); // Windows no proporciona fácilmente esta info con wmic
                process.setStatus("Running");
                process.setDiskUsage(0.0); // No es fácil obtener esto por proceso en Windows
                
                processes.add(process);
            } catch (NumberFormatException e) {
                // Ignorar líneas con formato incorrecto
                logger.debug("Error al parsear línea de proceso: {}", line);
            }
        }
        
        return processes;
    }
    
    /**
     * Recolecta información de procesos en Linux
     */
    private List<ProcessInfo> collectLinuxProcesses() throws Exception {
        List<ProcessInfo> processes = new ArrayList<>();
        
        // Comando para listar procesos con sus detalles
        String command = "ps aux --sort=-%cpu";
        String output = commandExecutor.executeCommand(command);
        
        // Expresión regular para extraer los campos
        Pattern pattern = Pattern.compile(
                "^\\s*(\\S+)\\s+" +    // USER
                "(\\d+)\\s+" +         // PID
                "(\\S+)\\s+" +         // %CPU
                "(\\S+)\\s+" +         // %MEM
                "(\\S+)\\s+" +         // VSZ
                "(\\S+)\\s+" +         // RSS
                "(\\S+)\\s+" +         // TTY
                "(\\S+)\\s+" +         // STAT
                "(\\S+)\\s+" +         // START
                "(\\S+)\\s+" +         // TIME
                "(.*)$");              // COMMAND
        
        String[] lines = output.split("\n");
        // Saltar la primera línea (encabezados)
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            Matcher matcher = pattern.matcher(line);
            
            if (matcher.find()) {
                try {
                    String username = matcher.group(1);
                    String processId = matcher.group(2);
                    double cpuUsage = Double.parseDouble(matcher.group(3));
                    double memoryUsage = Double.parseDouble(matcher.group(4));
                    String status = matcher.group(8);
                    String processName = matcher.group(11);
                    
                    // Abreviar nombre del proceso si es muy largo
                    if (processName.length() > 30) {
                        processName = processName.substring(0, 27) + "...";
                    }
                    
                    ProcessInfo process = new ProcessInfo(processId, processName, cpuUsage, memoryUsage);
                    process.setUsername(username);
                    process.setStatus(status);
                    process.setDiskUsage(0.0); // No es fácil obtener esto por proceso
                    
                    processes.add(process);
                } catch (NumberFormatException e) {
                    // Ignorar líneas con formato incorrecto
                    logger.debug("Error al parsear línea de proceso: {}", line);
                }
            }
        }
        
        return processes;
    }
    
    /**
     * Obtiene los procesos más pesados del sistema
     * @param limit Número máximo de procesos a obtener
     * @return Lista de procesos ordenados por uso de CPU
     */
    public List<ProcessInfo> getHeavyProcesses(int limit) {
        List<ProcessInfo> allProcesses = collectProcesses();
        
        // Ordenar por uso de CPU (descendente) y limitar resultado
        return allProcesses.stream()
                .sorted((p1, p2) -> Double.compare(p2.getCpuUsage(), p1.getCpuUsage()))
                .limit(limit)
                .toList();
    }
    
    // Utilidades
    private boolean isWindows() {
        return osName.contains("win");
    }
}