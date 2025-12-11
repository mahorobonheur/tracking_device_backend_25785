package mahoro.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import mahoro.backend.model.Location;
import mahoro.backend.model.RoleType;
import mahoro.backend.model.User;
import mahoro.backend.repository.LocationRepository;
import mahoro.backend.repository.UserRepository;
import mahoro.backend.service.UserService;

@Slf4j
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserRepository userRepository;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        log.info("Current authentication: {}", authentication);
        log.info("Principal class: {}", authentication != null ? authentication.getPrincipal().getClass().getName() : "null");
        
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            log.info("User is not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("authenticated", false));
        }

        if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            
            log.info("OAuth2 user email: {}", email);
            
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("authenticated", false, "message", "No email found in OAuth2 user"));
            }

            // Find user in database
            User user = userRepository.findByEmail(email).orElse(null);
            
            if (user == null) {
                log.info("User not found in database for email: {}, creating now...", email);
                
                // Auto-create user
                user = new User();
                user.setEmail(email);
                user.setFullName(name);
                user.setActive(false);
                user.setLocationAssigned(false);
                user.setRole(RoleType.USER);
                
                user = userRepository.save(user);
                log.info("Auto-created user: {}", user.getEmail());
            }

            log.info("User found/created in database: {}", user.getEmail());
            
            // Create session
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            session.setMaxInactiveInterval(1800); 
            
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", true);
            response.put("userId", user.getUserId());
            response.put("email", user.getEmail());
            response.put("fullName", user.getFullName());
            response.put("active", user.isActive());
            response.put("locationAssigned", user.isLocationAssigned());
            response.put("role", user.getRole() != null ? user.getRole().toString() : null);
            response.put("registered", true);
            
            log.info("Returning user response: {}", response);
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("authenticated", false));
    }

    // New endpoint to assign location to user
    @PostMapping("/users/{userId}/assign-location")
    public ResponseEntity<?> assignLocation(@PathVariable UUID userId, @RequestBody Map<String, String> request) {
        try {
            String locationId = request.get("locationId");
            
            if (locationId == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Location ID is required"));
            }
            
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Location location = locationRepository.findById(UUID.fromString(locationId))
                .orElseThrow(() -> new RuntimeException("Location not found"));
            
            // Check if location is a VILLAGE (lowest level)
            if (location.getType() != mahoro.backend.model.LocationType.VILLAGE) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Please select a village (lowest level location)"));
            }
            
            user.setAssignedLocation(location);
            user.setLocationAssigned(true);
            userRepository.save(user);
            
            // Generate and send OTP
            String otp = userService.createUserWithOtp(user);
            
            return ResponseEntity.ok(Map.of(
                "message", "Location assigned successfully. OTP has been sent to your email.",
                "locationAssigned", true,
                "otp", otp // In production, don't return OTP in response
            ));
            
        } catch (Exception e) {
            log.error("Error assigning location: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    // New endpoint to get location hierarchy
    @GetMapping("/locations/hierarchy")
    public ResponseEntity<?> getLocationHierarchy() {
        try {
            // Get all provinces
            List<Location> provinces = locationRepository.findByType(
                mahoro.backend.model.LocationType.PROVINCE
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("provinces", provinces);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/users/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String otp = request.get("otp");

            User user = userService.verifyOtp(email, otp);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Account verified successfully");
            response.put("email", user.getEmail());
            response.put("userId", user.getUserId().toString());
            response.put("active", String.valueOf(user.isActive()));
            response.put("locationAssigned", String.valueOf(user.isLocationAssigned()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            SecurityContextHolder.clearContext();
            
            return ResponseEntity.ok(Map.of(
                "message", "Logged out successfully",
                "success", true
            ));
        } catch (Exception e) {
            log.error("Error during logout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Logout failed"));
        }
    }

    @PostMapping("/users/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String fullName = request.get("fullName");
            
            // Check if user already exists
            if (userRepository.existsByEmail(email)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "User with this email already exists"));
            }
            
            User user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setActive(false);
            user.setRole(RoleType.USER);
            
            // Save user first
            user = userRepository.save(user);
            
            // Generate OTP
            String otp = userService.createUserWithOtp(user);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "OTP generated. Verify your account using this OTP.");
            response.put("otp", otp); // In production, send via email only
            response.put("userId", user.getUserId().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    // Add this to UserController.java
@PostMapping("/users/{userId}/resend-otp")
public ResponseEntity<?> resendOtp(@PathVariable UUID userId) {
    try {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user has location assigned
        if (!user.isLocationAssigned()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Please assign a location first"));
        }
        
        // Generate and send new OTP
        String otp = userService.createUserWithOtp(user);
        
        return ResponseEntity.ok(Map.of(
            "message", "New OTP has been sent to your email",
            "email", user.getEmail()
        ));
        
    } catch (Exception e) {
        log.error("Error resending OTP: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", e.getMessage()));
    }
}

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/users")
    public ResponseEntity<Page<User>> getAllUsers(Pageable pageable) {
        Page<User> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/search")
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

    @PutMapping("/users/{userId}/role")
public ResponseEntity<?> updateUserRole(@PathVariable UUID userId, @RequestBody Map<String, String> request) {
    try {
        String role = request.get("role");
        
        if (role == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Role is required"));
        }
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        try {
            RoleType roleType = RoleType.valueOf(role);
            user.setRole(roleType);
            userRepository.save(user);
            
            return ResponseEntity.ok(Map.of(
                "message", "Role updated successfully",
                "role", role
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid role type"));
        }
        
    } catch (Exception e) {
        log.error("Error updating user role: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", e.getMessage()));
    }
}

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable UUID id,
            @RequestBody User userDetails,
            @RequestParam(required = false) UUID newLocationId) {
        User updatedUser = userService.updateUser(id, userDetails, newLocationId);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}