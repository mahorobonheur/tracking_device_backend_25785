package mahoro.backend.service;

import mahoro.backend.model.Device;
import mahoro.backend.model.Location;
import mahoro.backend.model.LocationHistory;
import mahoro.backend.model.TrackingAlert;
import mahoro.backend.model.User;
import mahoro.backend.repository.LocationHistoryRepository;
import mahoro.backend.repository.DeviceRepository;
import mahoro.backend.repository.UserRepository;
import mahoro.backend.repository.TrackingAlertRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LocationTrackingService {

    private static final Logger log = LoggerFactory.getLogger(LocationTrackingService.class);

    private final LocationHistoryRepository locationHistoryRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final GeocodingService geocodingService; 
    private final TrackingAlertRepository trackingAlertRepository; 

    @Value("${app.geofence.radius-km:0.5}") 
    private double GEOFENCE_RADIUS_KM;


    public LocationTrackingService(
            LocationHistoryRepository locationHistoryRepository,
            DeviceRepository deviceRepository,
            UserRepository userRepository,
            GeocodingService geocodingService,
            TrackingAlertRepository trackingAlertRepository) {
        this.locationHistoryRepository = locationHistoryRepository;
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
        this.geocodingService = geocodingService;
        this.trackingAlertRepository = trackingAlertRepository;
    }

    @Transactional
public LocationHistory reportLocation(
        UUID deviceId,
        double latitude,
        double longitude,
        String recordedAddress,
        UUID recordedById) {

    Device device = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new IllegalArgumentException("Device not found with ID: " + deviceId));

    User recordedBy = null;
    if (recordedById != null) {
        recordedBy = userRepository.findById(recordedById)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + recordedById));
    }

    if (recordedAddress == null || recordedAddress.trim().isEmpty()) {
        recordedAddress = geocodingService.reverseGeocode(latitude, longitude);
    }

    LocationHistory history = new LocationHistory();
    history.setDevice(device);
    history.setLatitude(latitude);
    history.setLongitude(longitude);
    history.setRecordedAddress(recordedAddress);
    history.setRecordedBy(recordedBy);

    device.setLastReportedAt(history.getTimestamp());
    deviceRepository.save(device);

    history = locationHistoryRepository.save(history);

   
    checkGeoBoundaryAlert(device, latitude, longitude);

    List<TrackingAlert> alerts = trackingAlertRepository.findByDeviceAndCreatedAtAfter(
            device, history.getTimestamp().minusSeconds(1));
    history.setAlerts(alerts);

    return history;
}


    
   public LocationHistory getDeviceLastLocation(UUID deviceId) {
    Device device = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new IllegalArgumentException("Device not found with ID: " + deviceId));

    Pageable pageable = PageRequest.of(0, 1); 
    List<LocationHistory> latestList = locationHistoryRepository.findLatestByDevice(device, pageable);

    if (latestList.isEmpty()) {
        throw new IllegalArgumentException("No location history found for device ID: " + deviceId);
    }

    return latestList.get(0); 
   }


    public List<LocationHistory> getDeviceHistory(UUID deviceId, LocalDateTime from, LocalDateTime to) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found with ID: " + deviceId));

        if (from != null && to != null) {
            return locationHistoryRepository.findByDeviceAndTimestampBetweenOrderByTimestampAsc(device, from, to);
        } else {
            return locationHistoryRepository.findByDeviceOrderByTimestampAsc(device);
        }
    }


    private void checkGeoBoundaryAlert(Device device, double lat, double lon) {
        
        if (device.getPrimaryUser() == null || device.getPrimaryUser().getAssignedLocation() == null) {
            return; 
        }

        Location assignedArea = device.getPrimaryUser().getAssignedLocation();
        
       if (assignedArea.getCenterLatitude() != null && assignedArea.getCenterLongitude() != null) {

            double centerLat = assignedArea.getCenterLatitude();
            double centerLon = assignedArea.getCenterLongitude();
            
            double distance = calculateHaversineDistance(centerLat, centerLon, lat, lon);

            if (distance > GEOFENCE_RADIUS_KM) {
                logGeoBoundaryAlert(device, lat, lon, assignedArea.getName(), distance);
            }
        } else {
            log.warn("Assigned location {} has no coordinates defined for geofencing.", assignedArea.getName());
        }
    }


    private void logGeoBoundaryAlert(Device device, double lat, double lon, String locationName, double distance) {
        TrackingAlert alert = new TrackingAlert();
        alert.setDevice(device);
        alert.setLatitude(lat);
        alert.setLongitude(lon);
        alert.setMessage(
            String.format("Device (IMEI: %s) left assigned area '%s'. Distance from center: %.2f km.", 
                          device.getImei(), locationName, distance)
        );
        trackingAlertRepository.save(alert);
        log.info("Geo-boundary alert logged for Device {} at ({}, {})", device.getDeviceId(), lat, lon);
    }
    
    
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; 
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; 
    }
}