package mahoro.backend.service;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import mahoro.backend.model.Location;
import mahoro.backend.model.RoleType;
import mahoro.backend.model.User;
import mahoro.backend.repository.LocationRepository;
import mahoro.backend.repository.UserRepository;

@Slf4j
@Service
public class UserService {

      private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    private final Map<String, String> otpMap = new ConcurrentHashMap<>();

    public UserService(UserRepository userRepository,
                       LocationRepository locationRepository,
                       PasswordEncoder passwordEncoder,
                       JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    @Transactional
    public String createUserWithOtp(User user) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpMap.put(user.getEmail(), otp);
        sendOtpEmail(user, otp);
        log.info("Generated OTP {} for user {}", otp, user.getEmail());
        return otp;
    }

    private void sendOtpEmail(User user, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("MAHORO - Account Activation OTP");
            message.setText(
                "Dear " + user.getFullName() + ",\n\n" +
                "Thank you for registering with Device Tracking Application!\n\n" +
                "Your OTP for account activation is: " + otp + "\n\n" +
                "This OTP will expire in 10 minutes.\n\n" +
                "Please enter this code in the activation page to complete your registration.\n\n" +
                "Best regards,\n" +
                "MAHORO Team\n\n" +
                "Note: If you didn't request this, please ignore this email."
            );

            mailSender.send(message);
            log.info("OTP email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", user.getEmail(), e.getMessage());
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage());
        }
    }

    @Transactional
    public User verifyOtp(String email, String otp) {
        String storedOtp = otpMap.get(email);
        if (storedOtp != null && storedOtp.equals(otp)) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            // Check if user has assigned location
            if (!user.isLocationAssigned()) {
                throw new IllegalArgumentException("Please assign a location before activating your account");
            }
            
            user.setActive(true);
            userRepository.save(user);
            otpMap.remove(email);
            log.info("User {} activated successfully", email);
            return user;
        } else {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
    }

      public Page<User> searchUsers(String query, Pageable pageable) {
        return userRepository.searchUsers(query, pageable);
    }


    public User getUserById(UUID id) {
        return userRepository.findByUserId(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<User> findUsersByLocation(UUID locationId, Pageable pageable) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with ID: " + locationId));
        return userRepository.findByAssignedLocation(location, pageable);
    }

    public Page<User> findUsersByRole(RoleType role, Pageable pageable) {
        return userRepository.findByRole(role, pageable);
    }

    @Transactional
    public User updateUser(UUID id, User userDetails, UUID newLocationId) {
        User existingUser = getUserById(id);

        if (!existingUser.getEmail().equals(userDetails.getEmail()) &&
                userRepository.existsByEmail(userDetails.getEmail())) {
            throw new IllegalArgumentException("Email " + userDetails.getEmail() + " is already taken by another user.");
        }

        if (newLocationId != null &&
                (existingUser.getAssignedLocation() == null ||
                        !existingUser.getAssignedLocation().getLocationId().equals(newLocationId))) {

            Location newLocation = locationRepository.findById(newLocationId)
                    .orElseThrow(() -> new IllegalArgumentException("New assigned location not found with ID: " + newLocationId));
            existingUser.setAssignedLocation(newLocation);
        }

        existingUser.setFullName(userDetails.getFullName());
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setRole(userDetails.getRole());
        

        return userRepository.save(existingUser);
    }

    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }
}
