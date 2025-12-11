package mahoro.backend.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mahoro.backend.model.Device;
import mahoro.backend.model.User;
import mahoro.backend.repository.DeviceRepository;
import mahoro.backend.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {
    
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public Device createDevice(Device device) {
        try {
            log.info("Creating device: {}", device.getImei());
            
            // Check if device with same IMEI already exists
            if (device.getImei() != null) {
                Optional<Device> existing = deviceRepository.findByImei(device.getImei());
                if (existing.isPresent()) {
                    throw new RuntimeException("Device with IMEI " + device.getImei() + " already exists");
                }
            }
            
            // Check if device with same serial number already exists
            if (device.getSerialNumber() != null) {
                Optional<Device> existing = deviceRepository.findBySerialNumber(device.getSerialNumber());
                if (existing.isPresent()) {
                    throw new RuntimeException("Device with serial number " + device.getSerialNumber() + " already exists");
                }
            }
            
            // Set default values
            device.setRegistrationDate(LocalDateTime.now());
            device.setLastReportedAt(LocalDateTime.now());
            
            // If user is provided, fetch and set the user
            if (device.getUser() != null && device.getUser().getUserId() != null) {
                Optional<User> user = userRepository.findById(device.getUser().getUserId());
                user.ifPresent(device::setUser);
            }
            
            Device savedDevice = deviceRepository.save(device);
            log.info("Device created successfully: {}", savedDevice.getDeviceId());
            return savedDevice;
            
        } catch (Exception e) {
            log.error("Error creating device: ", e);
            throw new RuntimeException("Failed to create device: " + e.getMessage());
        }
    }
    
    public Device getDeviceById(UUID id) {
        try {
            return deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found with ID: " + id));
        } catch (Exception e) {
            log.error("Error fetching device by ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to fetch device: " + e.getMessage());
        }
    }
    
    public Page<Device> getAllDevices(Pageable pageable) {
        try {
            return deviceRepository.findAll(pageable);
        } catch (Exception e) {
            log.error("Error fetching all devices: ", e);
            throw new RuntimeException("Failed to fetch devices: " + e.getMessage());
        }
    }
    
    public Page<Device> searchDevices(String searchTerm, Pageable pageable) {
        try {
            log.info("Searching devices with term: '{}'", searchTerm);
            
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return getAllDevices(pageable);
            }
            
            String trimmedTerm = searchTerm.trim();
            log.debug("Using trimmed search term: '{}'", trimmedTerm);
            
            Page<Device> results = deviceRepository.searchDevices(trimmedTerm, pageable);
            log.info("Found {} devices for search term '{}'", results.getTotalElements(), trimmedTerm);
            
            return results;
            
        } catch (Exception e) {
            log.error("Error searching devices with term '{}': {}", searchTerm, e.getMessage(), e);
            throw new RuntimeException("Failed to search devices: " + e.getMessage());
        }
    }
    
    public Optional<Device> findDeviceByImei(String imei) {
        try {
            return deviceRepository.findByImei(imei);
        } catch (Exception e) {
            log.error("Error finding device by IMEI: ", e);
            throw new RuntimeException("Failed to find device: " + e.getMessage());
        }
    }
    
    public Optional<Device> findDeviceBySerialNumber(String serialNumber) {
        try {
            return deviceRepository.findBySerialNumber(serialNumber);
        } catch (Exception e) {
            log.error("Error finding device by serial number: ", e);
            throw new RuntimeException("Failed to find device: " + e.getMessage());
        }
    }
    
    public Page<Device> getDevicesByUser(UUID userId, Pageable pageable) {
        try {
            return deviceRepository.findByUser_UserId(userId, pageable);
        } catch (Exception e) {
            log.error("Error fetching devices for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to fetch devices: " + e.getMessage());
        }
    }
    
    public Map<String, Object> getDeviceStats(UUID userId) {
        Map<String, Object> stats = new HashMap<>();
        
        List<Device> devices = deviceRepository.findByUser_UserId(userId);
        
        long total = devices.size();
        long online = devices.stream()
            .filter(d -> d.getLastReportedAt() != null && 
                        d.getLastReportedAt().isAfter(LocalDateTime.now().minusMinutes(5)))
            .count();
        long offline = total - online;
        
        stats.put("total", total);
        stats.put("online", online);
        stats.put("offline", offline);
        stats.put("alerts", 0);
        
        return stats;
    }
    
    @Transactional
    public Device updateDevice(UUID id, Device deviceDetails) {
        try {
            Device device = getDeviceById(id);
            
            // Update fields
            if (deviceDetails.getDeviceName() != null) {
                device.setDeviceName(deviceDetails.getDeviceName());
            }
            if (deviceDetails.getModel() != null) {
                device.setModel(deviceDetails.getModel());
            }
            if (deviceDetails.getDeviceType() != null) {
                device.setDeviceType(deviceDetails.getDeviceType());
            }
            
            // Handle user update
            if (deviceDetails.getUser() != null && deviceDetails.getUser().getUserId() != null) {
                Optional<User> user = userRepository.findById(deviceDetails.getUser().getUserId());
                user.ifPresent(device::setUser);
            } else if (deviceDetails.getUser() == null) {
                // If user is explicitly set to null, clear the user
                device.setUser(null);
            }
            
            // Update IMEI and serial number if provided (but check for uniqueness)
            if (deviceDetails.getImei() != null && !deviceDetails.getImei().equals(device.getImei())) {
                if (deviceRepository.existsByImei(deviceDetails.getImei())) {
                    throw new RuntimeException("IMEI already exists: " + deviceDetails.getImei());
                }
                device.setImei(deviceDetails.getImei());
            }
            
            if (deviceDetails.getSerialNumber() != null && !deviceDetails.getSerialNumber().equals(device.getSerialNumber())) {
                if (deviceRepository.existsBySerialNumber(deviceDetails.getSerialNumber())) {
                    throw new RuntimeException("Serial number already exists: " + deviceDetails.getSerialNumber());
                }
                device.setSerialNumber(deviceDetails.getSerialNumber());
            }
            
            return deviceRepository.save(device);
        } catch (Exception e) {
            log.error("Error updating device {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to update device: " + e.getMessage());
        }
    }
    
    public List<Device> getDevicesWithRecentActivity(UUID userId) {
        try {
            List<Device> devices = deviceRepository.findByUser_UserId(userId);
            devices.sort((d1, d2) -> {
                LocalDateTime t1 = d1.getLastReportedAt() != null ? d1.getLastReportedAt() : LocalDateTime.MIN;
                LocalDateTime t2 = d2.getLastReportedAt() != null ? d2.getLastReportedAt() : LocalDateTime.MIN;
                return t2.compareTo(t1);
            });
            
            return devices.stream().limit(10).toList();
        } catch (Exception e) {
            log.error("Error fetching devices with recent activity for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to fetch devices: " + e.getMessage());
        }
    }
    
    @Transactional
    public void deleteDevice(UUID id) {
        try {
            if (!deviceRepository.existsById(id)) {
                throw new RuntimeException("Device not found with ID: " + id);
            }
            
            deviceRepository.deleteById(id);
            log.info("Device deleted: {}", id);
        } catch (Exception e) {
            log.error("Error deleting device {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete device: " + e.getMessage());
        }
    }

    public List<Device> getDevicesByUserId(UUID userId) {
        try {
            return deviceRepository.findByUser_UserId(userId);
        } catch (Exception e) {
            log.error("Error fetching devices for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to fetch devices: " + e.getMessage());
        }
    }
    
    public long countByUserId(UUID userId) {
        try {
            return deviceRepository.countByUserId(userId);
        } catch (Exception e) {
            log.error("Error counting devices for user {}: {}", userId, e.getMessage());
            return 0;
        }
    }
}