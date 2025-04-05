INSERT INTO alert_configurations (
  component_name, threshold_value, alert_interval, cpu_threshold, 
  memory_threshold, disk_threshold, created_at, is_active, is_enabled, name
) VALUES 
('CPU', 80.0, 30, 80.0, 0.0, 0.0, NOW(), true, true, 'Umbral de CPU'),
('RAM', 80.0, 30, 0.0, 80.0, 0.0, NOW(), true, true, 'Umbral de RAM'),
('DISK', 80.0, 30, 0.0, 0.0, 80.0, NOW(), true, true, 'Umbral de Disco');