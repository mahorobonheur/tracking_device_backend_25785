package mahoro.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mahoro.backend.model.Assignment;
import mahoro.backend.model.Device;
import mahoro.backend.model.User;
import mahoro.backend.repository.AssignmentRepository;
import mahoro.backend.repository.DeviceRepository;
import mahoro.backend.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;

    @Transactional
    public Assignment assignDevice(UUID userId, UUID deviceId, String reason) {
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

        // Check if device is already assigned
        Assignment currentAssignment = assignmentRepository.findByDeviceAndReturnDateIsNull(device);
        if (currentAssignment != null) {
            throw new IllegalStateException("Device is currently assigned to user: " + currentAssignment.getUser().getFullName() + ". Return it first.");
        }

        // Check if user has too many assignments (optional limit)
        List<Assignment> userAssignments = assignmentRepository.findByUserAndReturnDateIsNull(user);
        if (userAssignments.size() >= 10) { // Limit of 10 active assignments per user
            throw new IllegalStateException("User already has maximum number of assigned devices (10). Please return some devices first.");
        }

        Assignment newAssignment = new Assignment();
        newAssignment.setUser(user);
        newAssignment.setDevice(device);
        newAssignment.setReason(reason);
        newAssignment.setAssignmentDate(LocalDateTime.now());

        Assignment savedAssignment = assignmentRepository.save(newAssignment);
        log.info("Device {} assigned to user {} with reason: {}", deviceId, userId, reason);
        
        return savedAssignment;
    }

    @Transactional
    public Assignment returnDevice(UUID deviceId) {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) {
            throw new IllegalArgumentException("Device not found with ID: " + deviceId);
        }
        Device device = deviceOpt.get();

        Assignment currentAssignment = assignmentRepository.findByDeviceAndReturnDateIsNull(device);

        if (currentAssignment == null) {
            throw new IllegalArgumentException("Device is not currently assigned to any user.");
        }

        currentAssignment.setReturnDate(LocalDateTime.now());
        Assignment savedAssignment = assignmentRepository.save(currentAssignment);
        log.info("Device {} returned by user {}", deviceId, currentAssignment.getUser().getUserId());
        
        return savedAssignment;
    }

    public List<Assignment> findCurrentAssignmentsByPerson(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
        User user = userOpt.get();

        List<Assignment> assignments = assignmentRepository.findByUserAndReturnDateIsNull(user);
        log.info("Found {} current assignments for user {}", assignments.size(), userId);
        
        return assignments;
    }

    // New method: Get all active assignments
    public List<Assignment> findAllActiveAssignments() {
        List<Assignment> allAssignments = assignmentRepository.findAll();
        return allAssignments.stream()
                .filter(assignment -> assignment.getReturnDate() == null)
                .collect(Collectors.toList());
    }

    // New method: Get all assignments
    public List<Assignment> findAllAssignments() {
        return assignmentRepository.findAll();
    }
}