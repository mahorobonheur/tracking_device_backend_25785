package mahoro.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import mahoro.backend.model.Assignment;
import mahoro.backend.service.AssignmentService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

   
    @PostMapping("/assign")
    public ResponseEntity<Assignment> assignDevice(@RequestParam UUID personId,
                                                   @RequestParam UUID deviceId,
                                                   @RequestBody String reason) {
        Assignment assignment = assignmentService.assignDevice(personId, deviceId, reason);
        return new ResponseEntity<>(assignment, HttpStatus.CREATED);
    }

   
    @PostMapping("/return/{deviceId}")
    public ResponseEntity<Assignment> returnDevice(@PathVariable UUID deviceId) {
        Assignment assignment = assignmentService.returnDevice(deviceId);
        return ResponseEntity.ok(assignment);
    }

    @GetMapping("/current")
    public ResponseEntity<List<Assignment>> getCurrentAssignmentsByPerson(@RequestParam UUID personId) {
        List<Assignment> assignments = assignmentService.findCurrentAssignmentsByPerson(personId);
        return ResponseEntity.ok(assignments);
    }
}