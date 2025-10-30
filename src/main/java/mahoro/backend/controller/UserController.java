package mahoro.backend.controller;

import mahoro.backend.model.User;
import mahoro.backend.model.RoleType;
import mahoro.backend.service.UserService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody Map<String, String> request) {
        try {
            User user = new User();
            user.setFullName(request.get("fullName"));
            user.setEmail(request.get("email"));
            user.setPassword(request.get("password"));

            UUID locationId = UUID.fromString(request.get("locationId"));
            String otp = userService.createUserWithOtp(user, locationId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "OTP generated. Verify your account using this OTP.");
            response.put("otp", otp); 
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

   
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String otp = request.get("otp");
            User user = userService.verifyOtp(email, otp);
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            User user = userService.login(email, password);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<Page<User>> getAllUsers(Pageable pageable) {
        Page<User> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<User>> searchUsers(
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) RoleType role,
            Pageable pageable) {

        if (locationId != null) {
            Page<User> users = userService.findUsersByLocation(locationId, pageable);
            return ResponseEntity.ok(users);
        }

        if (role != null) {
            Page<User> users = userService.findUsersByRole(role, pageable);
            return ResponseEntity.ok(users);
        }

        return ResponseEntity.badRequest().body(Page.empty(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable UUID id,
            @RequestBody User userDetails,
            @RequestParam(required = false) UUID newLocationId) {
        User updatedUser = userService.updateUser(id, userDetails, newLocationId);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
