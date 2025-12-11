package mahoro.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mahoro.backend.model.LocationHistory;
import mahoro.backend.service.LocationTrackingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final LocationTrackingService locationTrackingService;

    @PostMapping("/report-location")
    public ResponseEntity<?> reportLocation(@RequestBody Map<String, Object> request) {
        try {
            UUID deviceId = UUID.fromString((String) request.get("deviceId"));
            Double latitude = Double.parseDouble(request.get("latitude").toString());
            Double longitude = Double.parseDouble(request.get("longitude").toString());
            String address = (String) request.get("address");
            
            LocationHistory location = locationTrackingService.reportLocation(deviceId, latitude, longitude, address);
            
            return ResponseEntity.ok(Map.of(
                "message", "Location reported successfully",
                "locationId", location.getLocationHistoryId(),
                "timestamp", location.getTimestamp()
            ));
        } catch (Exception e) {
            log.error("Error reporting location: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/report-location-by-imei")
    public ResponseEntity<?> reportLocationByImei(@RequestBody Map<String, Object> request) {
        try {
            String imei = (String) request.get("imei");
            Double latitude = Double.parseDouble(request.get("latitude").toString());
            Double longitude = Double.parseDouble(request.get("longitude").toString());
            
            LocationHistory location = locationTrackingService.reportLocationByImei(imei, latitude, longitude);
            
            return ResponseEntity.ok(Map.of(
                "message", "Location reported successfully",
                "deviceId", location.getDevice().getDeviceId(),
                "timestamp", location.getTimestamp()
            ));
        } catch (Exception e) {
            log.error("Error reporting location by IMEI: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/device/{deviceId}/current-location")
    public ResponseEntity<?> getCurrentLocation(@PathVariable UUID deviceId) {
        try {
            return locationTrackingService.getLatestLocation(deviceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/device/{deviceId}/history")
    public ResponseEntity<?> getLocationHistory(
            @PathVariable UUID deviceId,
            @RequestParam(defaultValue = "24") int hours) {
        try {
            List<LocationHistory> history = locationTrackingService.getLocationHistory(deviceId, hours);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/device/{deviceId}/all-locations")
    public ResponseEntity<?> getAllLocations(@PathVariable UUID deviceId) {
        try {
            List<LocationHistory> history = locationTrackingService.getAllLocations(deviceId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/device/{deviceId}/status")
    public ResponseEntity<?> getDeviceStatus(@PathVariable UUID deviceId) {
        try {
            var latestLocation = locationTrackingService.getLatestLocation(deviceId);
            
            Map<String, Object> status = new HashMap<>();
            status.put("deviceId", deviceId);
            
            if (latestLocation.isPresent()) {
                LocationHistory location = latestLocation.get();
                status.put("online", location.getTimestamp().isAfter(
                        java.time.LocalDateTime.now().minusMinutes(5)));
                status.put("lastSeen", location.getTimestamp());
                status.put("latitude", location.getLatitude());
                status.put("longitude", location.getLongitude());
                status.put("address", location.getRecordedAddress());
            } else {
                status.put("online", false);
                status.put("lastSeen", null);
                status.put("message", "No location data available");
            }
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
}