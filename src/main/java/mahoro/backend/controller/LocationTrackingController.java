package mahoro.backend.controller;

import mahoro.backend.model.LocationHistory;
import mahoro.backend.service.LocationTrackingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tracking")
public class LocationTrackingController {

    private final LocationTrackingService locationTrackingService;

    public LocationTrackingController(LocationTrackingService locationTrackingService) {
        this.locationTrackingService = locationTrackingService;
    }

    @PostMapping("/report")
public ResponseEntity<LocationHistory> reportLocation(
        @RequestParam UUID deviceId,
        @RequestParam double latitude,
        @RequestParam double longitude,
        @RequestParam(required = false) String recordedAddress,
        @RequestParam(required = false) UUID recordedById) {
    try {
        LocationHistory history = locationTrackingService.reportLocation(
                deviceId, latitude, longitude, recordedAddress, recordedById);
        return new ResponseEntity<>(history, HttpStatus.CREATED);
    } catch (IllegalArgumentException e) {
        return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
    } catch (Exception e) {
        return new ResponseEntity("Failed to report location: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

@GetMapping("/{deviceId}/last-location")
public ResponseEntity<LocationHistory> getLastLocation(@PathVariable UUID deviceId) {
    try {
        LocationHistory lastLocation = locationTrackingService.getDeviceLastLocation(deviceId);
        return ResponseEntity.ok(lastLocation);
    } catch (IllegalArgumentException e) {
        return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
    }
}

@GetMapping("/{deviceId}/history")
public ResponseEntity<List<LocationHistory>> getDeviceHistory(
        @PathVariable UUID deviceId,
        @RequestParam(required = false) LocalDateTime from,
        @RequestParam(required = false) LocalDateTime to) {
    try {
        List<LocationHistory> history = locationTrackingService.getDeviceHistory(deviceId, from, to);
        return ResponseEntity.ok(history);
    } catch (IllegalArgumentException e) {
        return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
    }
}

}