package mahoro.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import mahoro.backend.model.Device;
import mahoro.backend.service.DeviceService;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

   
    @PostMapping("/create")
    public ResponseEntity<Device> createDevice(@RequestBody Device device) {
        Device savedDevice = deviceService.createDevice(device);
        return new ResponseEntity<>(savedDevice, HttpStatus.CREATED);
    }

   
    @GetMapping("/get/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable UUID id) {
        Device device = deviceService.getDeviceById(id);
        return ResponseEntity.ok(device);
    }

  
    @GetMapping
    public ResponseEntity<Page<Device>> getAllDevices(Pageable pageable) {
        Page<Device> devices = deviceService.getAllDevices(pageable);
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchDevices(@RequestParam(required = false) String imei,
                                           @RequestParam(required = false) String serialNumber,
                                           @RequestParam(required = false) String model,
                                           Pageable pageable) {

        if (imei != null) {
            Optional<Device> device = deviceService.findDeviceByImei(imei);
            return device.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        }
        if (serialNumber != null) {
            Optional<Device> device = deviceService.findDeviceBySerialNumber(serialNumber);
            return device.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        }
        if (model != null) {
            Page<Device> devices = deviceService.searchDevicesByModel(model, pageable);
            return ResponseEntity.ok(devices);
        }

         return ResponseEntity.badRequest().body("Please provide IMEI, Serial Number, or Model for search.");
    }

   
    @PutMapping("/update/{id}")
    public ResponseEntity<Device> updateDevice(@PathVariable UUID id, @RequestBody Device deviceDetails) {
        Device updatedDevice = deviceService.updateDevice(id, deviceDetails);
        return ResponseEntity.ok(updatedDevice);
    }

   
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable UUID id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }
}