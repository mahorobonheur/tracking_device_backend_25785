package mahoro.backend.service;

import mahoro.backend.model.Location;
import mahoro.backend.model.User;
import mahoro.backend.model.RoleType;
import mahoro.backend.repository.LocationRepository;
import mahoro.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;

import java.util.UUID;
import java.util.Random;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    public String createUserWithOtp(User user, UUID locationId) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists.");
        }

        Location assignedLocation = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Assigned location not found with ID: " + locationId));

        user.setAssignedLocation(assignedLocation);
        user.setActive(false);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpMap.put(user.getEmail(), otp);

        // Send email
        sendOtpEmail(user.getEmail(), otp);

        return otp;
    }

    private void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Your Verification OTP");
            message.setText("Dear user,\n\nYour OTP for account verification is: " + otp + 
                            "\n\nPlease enter this code to activate your account.\n\nBest regards,\nTracking Device Team");

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send OTP email: " + e.getMessage());
        }
    }

    public User verifyOtp(String email, String otp) {
        String storedOtp = otpMap.get(email);
        if (storedOtp != null && storedOtp.equals(otp)) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            user.setActive(true);
            userRepository.save(user);
            otpMap.remove(email);
            return user;
        } else {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
    }

    public User login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.isActive()) {
            throw new IllegalArgumentException("Account not activated. Verify OTP first.");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        user.setPassword(null);
        return user;
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
        existingUser.setRoles(userDetails.getRoles());

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
