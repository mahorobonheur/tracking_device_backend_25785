package mahoro.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mahoro.backend.model.Device;
import mahoro.backend.model.User;
import mahoro.backend.repository.DeviceRepository;
import mahoro.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository personRepository;

    public DeviceService(DeviceRepository deviceRepository, UserRepository personRepository) {
        this.deviceRepository = deviceRepository;
        this.personRepository = personRepository;
    }

  
    @Transactional
    public Device createDevice(Device device) {
        if (deviceRepository.existsByImei(device.getImei())) {
            throw new IllegalArgumentException("Device with IMEI " + device.getImei() + " already exists.");
        }
        if (device.getSerialNumber() != null && deviceRepository.existsBySerialNumber(device.getSerialNumber())) {
            throw new IllegalArgumentException("Device with Serial Number " + device.getSerialNumber() + " already exists.");
        }
        device.setRegistrationDate(LocalDateTime.now());
        return deviceRepository.save(device);
    }

    public Device getDeviceById(UUID id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Device not found with ID: " + id));
    }

    
    public Page<Device> getAllDevices(Pageable pageable) {
        return deviceRepository.findAll(pageable);
    }

  
    public Optional<Device> findDeviceByImei(String imei) {
        return deviceRepository.findByImei(imei);
    }

    public Optional<Device> findDeviceBySerialNumber(String serialNumber) {
        return deviceRepository.findBySerialNumber(serialNumber);
    }

    public Page<Device> searchDevicesByModel(String model, Pageable pageable) {
        return deviceRepository.findByModelContainingIgnoreCase(model, pageable);
    }


    @Transactional
    public Device updateDevice(UUID id, Device deviceDetails) {
        Device existingDevice = getDeviceById(id); 
        if (!existingDevice.getImei().equals(deviceDetails.getImei()) && deviceRepository.existsByImei(deviceDetails.getImei())) {
            throw new IllegalArgumentException("IMEI " + deviceDetails.getImei() + " is already taken by another device.");
        }


        if (deviceDetails.getSerialNumber() != null && !deviceDetails.getSerialNumber().equals(existingDevice.getSerialNumber())
                && deviceRepository.existsBySerialNumber(deviceDetails.getSerialNumber())) {
            throw new IllegalArgumentException("Serial Number " + deviceDetails.getSerialNumber() + " is already taken by another device.");
        }

        existingDevice.setImei(deviceDetails.getImei());
        existingDevice.setSerialNumber(deviceDetails.getSerialNumber());
        existingDevice.setModel(deviceDetails.getModel());


        if (deviceDetails.getPrimaryUser() != null && deviceDetails.getPrimaryUser().getUserId() != null) {
             Optional<User> primaryUserOpt = personRepository.findById(deviceDetails.getPrimaryUser().getUserId());
             if (primaryUserOpt.isEmpty()) {
                 throw new IllegalArgumentException("Primary User not found with ID: " + deviceDetails.getPrimaryUser().getUserId());
             }
             existingDevice.setPrimaryUser(primaryUserOpt.get());
        } else if (deviceDetails.getPrimaryUser() == null) {
            existingDevice.setPrimaryUser(null);
        }

        return deviceRepository.save(existingDevice);
    }

 
    @Transactional
    public void deleteDevice(UUID id) {
        if (!deviceRepository.existsById(id)) {
            throw new IllegalArgumentException("Device not found with ID: " + id);
        }
        deviceRepository.deleteById(id);
    }
}