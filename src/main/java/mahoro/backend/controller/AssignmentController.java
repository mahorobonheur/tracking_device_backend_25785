package mahoro.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mahoro.backend.model.Assignment;
import mahoro.backend.service.AssignmentService;

@Slf4j
@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping("/assign")
    public ResponseEntity<?> assignDevice(@RequestParam UUID personId,
                                          @RequestParam UUID deviceId,
                                          @RequestBody String reason) {
        try {
            log.info("Assigning device {} to person {} with reason: {}", deviceId, personId, reason);
            Assignment assignment = assignmentService.assignDevice(personId, deviceId, reason);
            return ResponseEntity.ok(assignment);
        } catch (IllegalArgumentException e) {
            log.error("Validation error assigning device: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("State error assigning device: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error assigning device: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to assign device: " + e.getMessage()));
        }
    }

    @PostMapping("/return/{deviceId}")
    public ResponseEntity<?> returnDevice(@PathVariable UUID deviceId) {
        try {
            log.info("Returning device: {}", deviceId);
            Assignment assignment = assignmentService.returnDevice(deviceId);
            return ResponseEntity.ok(assignment);
        } catch (IllegalArgumentException e) {
            log.error("Validation error returning device: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error returning device: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to return device: " + e.getMessage()));
        }
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentAssignmentsByPerson(@RequestParam UUID personId) {
        try {
            log.info("Fetching current assignments for person: {}", personId);
            List<Assignment> assignments = assignmentService.findCurrentAssignmentsByPerson(personId);
            return ResponseEntity.ok(assignments);
        } catch (IllegalArgumentException e) {
            log.error("Validation error fetching assignments: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching assignments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to fetch assignments: " + e.getMessage()));
        }
    }

    // New endpoint: Get all active assignments
    @GetMapping("/active")
    public ResponseEntity<?> getAllActiveAssignments() {
        try {
            log.info("Fetching all active assignments");
            List<Assignment> assignments = assignmentService.findAllActiveAssignments();
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            log.error("Error fetching active assignments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to fetch active assignments: " + e.getMessage()));
        }
    }

    // New endpoint: Get all assignments (for admin)
    @GetMapping("/all")
    public ResponseEntity<?> getAllAssignments() {
        try {
            log.info("Fetching all assignments");
            List<Assignment> assignments = assignmentService.findAllAssignments();
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            log.error("Error fetching all assignments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to fetch all assignments: " + e.getMessage()));
        }
    }

    // Simple error response class
    static class ErrorResponse {
        private String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
        
        public String getError() {
            return error;
        }
        
        public void setError(String error) {
            this.error = error;
        }
    }
}