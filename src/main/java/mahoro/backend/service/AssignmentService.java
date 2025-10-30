package mahoro.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mahoro.backend.model.Assignment;
import mahoro.backend.model.Device;
import mahoro.backend.model.User;
import mahoro.backend.repository.AssignmentRepository;
import mahoro.backend.repository.DeviceRepository;
import mahoro.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;

    public AssignmentService(AssignmentRepository assignmentRepository, UserRepository userRepository, DeviceRepository deviceRepository) {
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
    }

   
    @Transactional
    public Assignment assignDevice(UUID userId, UUID  deviceId, String reason) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
        User user = userOpt.get();

        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) {
            throw new IllegalArgumentException("Device not found with ID: " + deviceId);
        }
        Device device = deviceOpt.get();

        Assignment currentAssignment = assignmentRepository.findByDeviceAndReturnDateIsNull(device);
        if (currentAssignment != null) {
            throw new IllegalStateException("Device is currently assigned to user: " + currentAssignment.getUser().getFullName() + ". Return it first.");
        }

        Assignment newAssignment = new Assignment();
        newAssignment.setUser(user);
        newAssignment.setDevice(device);
        newAssignment.setReason(reason);
        newAssignment.setAssignmentDate(LocalDateTime.now());

        return assignmentRepository.save(newAssignment);
    }

  
    @Transactional
    public Assignment returnDevice(UUID  deviceId) {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) {
            throw new IllegalArgumentException("Device not found with ID: " + deviceId);
        }
        Device device = deviceOpt.get();

        Assignment currentAssignment = assignmentRepository.findByDeviceAndReturnDateIsNull(device);

        if (currentAssignment == null) {
            throw new IllegalArgumentException("Device is not currently assigned to any person.");
        }

        currentAssignment.setReturnDate(LocalDateTime.now());
        return assignmentRepository.save(currentAssignment);
    }

   
    public List<Assignment> findCurrentAssignmentsByPerson(UUID  userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
        User user = userOpt.get();

        return assignmentRepository.findByUserAndReturnDateIsNull(user);
    }
}