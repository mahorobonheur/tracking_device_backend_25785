package mahoro.backend.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mahoro.backend.model.Device;
import mahoro.backend.service.DeviceService;

@Slf4j
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping("/create")
    public ResponseEntity<?> createDevice(@RequestBody Device device) {
        try {
            Device savedDevice = deviceService.createDevice(device);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", savedDevice);
            response.put("message", "Device created successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating device: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getDeviceById(@PathVariable UUID id) {
        try {
            log.info("Fetching device by ID: {}", id);
            Device device = deviceService.getDeviceById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", device);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching device by ID: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Device not found: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllDevices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "registrationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        try {
            Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            
            Page<Device> devices = deviceService.getAllDevices(pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", devices.getContent());
            response.put("totalElements", devices.getTotalElements());
            response.put("totalPages", devices.getTotalPages());
            response.put("currentPage", devices.getNumber());
            response.put("pageSize", devices.getSize());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching all devices: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchDevices(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "registrationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        try {
            log.info("Searching devices with term: '{}', page: {}, size: {}", q, page, size);
            
            Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            
            Page<Device> devices;
            
            if (q != null && !q.trim().isEmpty()) {
                devices = deviceService.searchDevices(q.trim(), pageable);
                log.info("Found {} devices for search term: '{}'", devices.getTotalElements(), q);
            } else {
                devices = deviceService.getAllDevices(pageable);
                log.info("No search term provided, returning all {} devices", devices.getTotalElements());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", devices.getContent());
            response.put("totalElements", devices.getTotalElements());
            response.put("totalPages", devices.getTotalPages());
            response.put("currentPage", devices.getNumber());
            response.put("pageSize", devices.getSize());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error searching devices: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Search failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateDevice(@PathVariable UUID id, @RequestBody Device deviceDetails) {
        try {
            Device updatedDevice = deviceService.updateDevice(id, deviceDetails);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedDevice);
            response.put("message", "Device updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating device: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteDevice(@PathVariable UUID id) {
        try {
            deviceService.deleteDevice(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Device deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting device: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}