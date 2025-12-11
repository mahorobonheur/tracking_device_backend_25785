package mahoro.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mahoro.backend.model.AlertType;
import mahoro.backend.model.Device;
import mahoro.backend.model.Location;
import mahoro.backend.model.LocationHistory;
import mahoro.backend.repository.DeviceRepository;
import mahoro.backend.repository.LocationHistoryRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationTrackingService {

    private final DeviceRepository deviceRepository;
    private final LocationHistoryRepository locationHistoryRepository;
    private final AlertService alertService;

    @Transactional
    public LocationHistory reportLocation(UUID deviceId, Double latitude, Double longitude, String address) {
        try {
            Device device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new RuntimeException("Device not found"));

            LocationHistory location = new LocationHistory();
            location.setDevice(device);
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            location.setRecordedAddress(address);
            location.setTimestamp(LocalDateTime.now());

            // Update device's last reported time
            device.setLastReportedAt(LocalDateTime.now());
            deviceRepository.save(device);

            // Check for geofence violations (if any geofence is set)
            checkGeofenceViolations(device, latitude, longitude);

            LocationHistory savedLocation = locationHistoryRepository.save(location);
            log.info("Location reported for device {}: {}, {}", deviceId, latitude, longitude);
            
            return savedLocation;
        } catch (Exception e) {
            log.error("Error reporting location for device {}: {}", deviceId, e.getMessage());
            throw new RuntimeException("Failed to report location: " + e.getMessage());
        }
    }

    @Transactional
    public LocationHistory reportLocationByImei(String imei, Double latitude, Double longitude) {
        Device device = deviceRepository.findByImei(imei)
                .orElseThrow(() -> new RuntimeException("Device not found with IMEI: " + imei));
        return reportLocation(device.getDeviceId(), latitude, longitude, null);
    }

    public List<LocationHistory> getLocationHistory(UUID deviceId, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return locationHistoryRepository.findByDevice_DeviceIdAndTimestampAfterOrderByTimestampDesc(
                deviceId, since);
    }

    public Optional<LocationHistory> getLatestLocation(UUID deviceId) {
        return locationHistoryRepository.findTopByDevice_DeviceIdOrderByTimestampDesc(deviceId);
    }

    public List<LocationHistory> getAllLocations(UUID deviceId) {
        return locationHistoryRepository.findByDevice_DeviceIdOrderByTimestampDesc(deviceId);
    }

    private void checkGeofenceViolations(Device device, Double latitude, Double longitude) {
        // Check if device is outside assigned location (geofence)
        if (device.getUser() != null && device.getUser().getAssignedLocation() != null) {
            Location assignedLocation = device.getUser().getAssignedLocation();
            if (assignedLocation.getCenterLatitude() != null && assignedLocation.getCenterLongitude() != null) {
                double distance = calculateDistance(
                        latitude, longitude,
                        assignedLocation.getCenterLatitude(), assignedLocation.getCenterLongitude()
                );
               
                if (distance > 1.0) { 
                    try {
                        alertService.createAlert(
                            device.getDeviceId(),
                            String.format("Device moved %,.2f km outside assigned location", distance),
                            AlertType.GEOFENCE_VIOLATION,
                            latitude,
                            longitude
                        );
                        log.warn("Geofence violation detected for device {}: moved {} km from assigned location", 
                                device.getDeviceId(), String.format("%.2f", distance));
                    } catch (Exception e) {
                        log.error("Failed to create geofence alert for device {}: {}", device.getDeviceId(), e.getMessage());
                    }
                }
            }
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    
        final int R = 6371; 
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }

   
    public void createMovementAlert(Device device, Double latitude, Double longitude) {
        try {
            alertService.createAlert(
                device.getDeviceId(),
                "Movement detected while device should be stationary",
                AlertType.MOVEMENT_DETECTED,
                latitude,
                longitude
            );
        } catch (Exception e) {
            log.error("Failed to create movement alert: {}", e.getMessage());
        }
    }

  
    public void createLowBatteryAlert(Device device, Double batteryLevel) {
        try {
             alertService.createAlert(
                device.getDeviceId(),
                String.format("Low battery: %.0f%%", batteryLevel),
                AlertType.LOW_BATTERY,
                null,
                null
            );
        } catch (Exception e) {
            log.error("Failed to create low battery alert: {}", e.getMessage());
        }
    }
}