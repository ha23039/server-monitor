package com.monitoring.server.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.monitoring.server.data.entity.SystemMetric;
import com.monitoring.server.data.repository.MetricRepository;
import com.monitoring.server.monitoring.collector.SystemMetricCollector;
import com.monitoring.server.service.interfaces.MetricService;

/**
 * Implementación del servicio de gestión de métricas del sistema
 */
@Service
public class MetricServiceImpl implements MetricService {

    private static final Logger logger = LoggerFactory.getLogger(MetricServiceImpl.class);
    
    @Autowired
    private MetricRepository metricRepository;
    
    @Autowired
    private SystemMetricCollector systemMetricCollector;
    
    @Override
    public SystemMetric save(SystemMetric metric) {
        return metricRepository.save(metric);
    }
    
    @Override
    public SystemMetric findById(Long id) {
        return metricRepository.findById(id).orElse(null);
    }
    
    @Override
    public List<SystemMetric> findAll() {
        return metricRepository.findAll();
    }
    
    @Override
    public List<SystemMetric> findByMetricName(String metricName) {
        return metricRepository.findByMetricName(metricName);
    }
    
    @Override
    public List<SystemMetric> findByTimestampBetween(LocalDateTime start, LocalDateTime end) {
        return metricRepository.findByTimestampBetween(start, end);
    }
    
    @Override
    @Transactional
    public int deleteOlderThan(LocalDateTime date) {
        return metricRepository.deleteByTimestampBefore(date);
    }
    
    @Override
    public SystemMetric getCurrentMetrics() {
        return systemMetricCollector.collectMetrics();
    }
    
    @Override
    public List<SystemMetric> getLatestMetrics() {
        PageRequest pageRequest = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "timestamp"));
        return metricRepository.findAll(pageRequest).getContent();
    }
    
    @Override
    public List<SystemMetric> getMetricsHistory(String period) {
        LocalDateTime startTime;
        LocalDateTime endTime = LocalDateTime.now();
        
        switch (period.toLowerCase()) {
            case "1h":
                startTime = endTime.minusHours(1);
                break;
            case "24h":
                startTime = endTime.minusHours(24);
                break;
            case "7d":
                startTime = endTime.minusDays(7);
                break;
            case "1m":
                startTime = endTime.minusMonths(1);
                break;
            default:
                startTime = endTime.minusHours(1);
                break;
        }
        
        return metricRepository.findByTimestampBetween(startTime, endTime);
    }
    
    @Override
    public List<SystemMetric> getMetricsWithAlerts() {
        return metricRepository.findByCpuAlertTrueOrMemoryAlertTrueOrDiskAlertTrue();
    }
}