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



}