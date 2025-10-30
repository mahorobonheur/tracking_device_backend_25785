package mahoro.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import mahoro.backend.model.Location;
import mahoro.backend.model.LocationType;
import mahoro.backend.service.LocationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    
    @PostMapping
    public ResponseEntity<Location> createLocation(@RequestBody Location location,
                                                   @RequestParam(required = false) UUID  parentId) {
        Location savedLocation = locationService.saveLocation(location, parentId);
        return new ResponseEntity<>(savedLocation, HttpStatus.CREATED);
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
        List<Location> children = locationService.findChildrenOf(id);
        return ResponseEntity.ok(children);
    }
}
