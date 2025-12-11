package mahoro.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mahoro.backend.model.AlertType;
import mahoro.backend.model.Device;
import mahoro.backend.model.TrackingAlert;
import mahoro.backend.repository.AlertRepository;
import mahoro.backend.repository.DeviceRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {
    
    private final AlertRepository alertRepository;
    private final DeviceRepository deviceRepository;

      @Transactional
    public TrackingAlert createAlert(UUID deviceId, String message, AlertType type, 
                                    Double latitude, Double longitude) {
        try {
            log.info("Creating alert for device: {}, type: {}, message: {}", deviceId, type, message);
            
            Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));
            
            TrackingAlert alert = new TrackingAlert();
            alert.setDevice(device);
            alert.setMessage(message);
            alert.setAlertType(type);
            alert.setLatitude(latitude);
            alert.setLongitude(longitude);
            alert.setCreatedAt(LocalDateTime.now());
            alert.setResolved(false);
            
            return alertRepository.save(alert);
            
        } catch (Exception e) {
            log.error("Error creating alert: ", e);
            throw new RuntimeException("Failed to create alert: " + e.getMessage());
        }
    }
    
    public TrackingAlert createGeofenceAlert(Device device, String message, Double latitude, Double longitude) {
        return createAlert(device.getDeviceId(), message, AlertType.GEOFENCE_VIOLATION, latitude, longitude);
    }
    

    public TrackingAlert createMovementAlert(Device device, String message, Double latitude, Double longitude) {
        return createAlert(device.getDeviceId(), message, AlertType.MOVEMENT_DETECTED, latitude, longitude);
    }

    public TrackingAlert createLowBatteryAlert(Device device, String message) {
        return createAlert(device.getDeviceId(), message, AlertType.LOW_BATTERY, null, null);
    }
    

    
    public Page<TrackingAlert> getAlerts(Pageable pageable) {
        return alertRepository.findAll(pageable);
    }
    
    public Page<TrackingAlert> searchAlerts(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return alertRepository.findAll(pageable);
        }
        
        String likePattern = "%" + searchTerm.toLowerCase() + "%";
        
        Specification<TrackingAlert> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            predicates.add(cb.like(cb.lower(root.get("message")), likePattern));
            
            return cb.or(predicates.toArray(new Predicate[0]));
        };
        
        return alertRepository.findAll(spec, pageable);
    }
    
    public List<TrackingAlert> getActiveAlertsByDevice(UUID deviceId) {
        return alertRepository.findByDevice_DeviceIdAndResolvedFalseOrderByCreatedAtDesc(deviceId);
    }
    
    public List<TrackingAlert> getActiveAlertsByUser(UUID userId) {
        return alertRepository.findByDevice_User_UserIdAndResolvedFalseOrderByCreatedAtDesc(userId);
    }
    
    public long getUnresolvedAlertCount(UUID userId) {
        return alertRepository.countByDevice_User_UserIdAndResolvedFalse(userId);
    }
    
    @Transactional
    public TrackingAlert markAlertAsResolved(UUID alertId) {
        TrackingAlert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new RuntimeException("Alert not found"));
        
        alert.setResolved(true);
        alert.setResolvedAt(LocalDateTime.now());
        
        return alertRepository.save(alert);
    }
    
    @Transactional
    public void deleteAlert(UUID alertId) {
        alertRepository.deleteById(alertId);
    }
    
    public Map<String, Long> getAlertStatistics(UUID userId) {
        Map<String, Long> stats = new HashMap<>();
        
        List<TrackingAlert> alerts = alertRepository.findByDevice_User_UserId(userId);
        
        long total = alerts.size();
        long unresolved = alerts.stream().filter(a -> !a.isResolved()).count();
        long geofence = alerts.stream().filter(a -> a.getAlertType() == AlertType.GEOFENCE_VIOLATION).count();
        long battery = alerts.stream().filter(a -> a.getAlertType() == AlertType.LOW_BATTERY).count();
        long movement = alerts.stream().filter(a -> a.getAlertType() == AlertType.MOVEMENT_DETECTED).count();
        
        stats.put("total", total);
        stats.put("unresolved", unresolved);
        stats.put("geofence", geofence);
        stats.put("battery", battery);
        stats.put("movement", movement);
        
        return stats;
    }
    
    
}