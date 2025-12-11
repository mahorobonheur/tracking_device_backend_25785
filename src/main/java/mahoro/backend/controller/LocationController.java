package mahoro.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import mahoro.backend.model.Location;
import mahoro.backend.model.LocationType;
import mahoro.backend.service.LocationService;

@Slf4j
@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }
    
    @PostMapping
    public ResponseEntity<Location> createLocation(@RequestBody Location location,
                                                   @RequestParam(required = false) UUID parentId) {
        Location savedLocation = locationService.saveLocation(location, parentId);
        return new ResponseEntity<>(savedLocation, HttpStatus.CREATED);
    }

     @PutMapping("/{id}")
    public ResponseEntity<Location> updateLocation(@PathVariable UUID id, 
                                                   @RequestBody Location location) {
        Location updatedLocation = locationService.updateLocation(id, location);
        return ResponseEntity.ok(updatedLocation);
    }

    @DeleteMapping("/{id}")
public ResponseEntity<Void> deleteLocation(@PathVariable UUID id) {
    locationService.deleteLocation(id);
    return ResponseEntity.noContent().build();
}
    
    @GetMapping
    public ResponseEntity<Page<Location>> getAllLocations(Pageable pageable) {
        Page<Location> locations = locationService.findAllLocations(pageable);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Location> getLocationById(@PathVariable UUID id) {
        Location location = locationService.findById(id);
        return ResponseEntity.ok(location);
    }
    
    @GetMapping("/type")
    public ResponseEntity<Page<Location>> getLocationsByType(@RequestParam LocationType type, Pageable pageable) {
        Page<Location> locations = locationService.findByType(type, pageable);
        return ResponseEntity.ok(locations);
    }
    
    @GetMapping("/{id}/children")
    public ResponseEntity<List<Location>> getChildren(@PathVariable UUID id) {
        log.info("Getting children for location ID: {}", id);
        List<Location> children = locationService.findChildrenOf(id);
        return ResponseEntity.ok(children);
    }
}