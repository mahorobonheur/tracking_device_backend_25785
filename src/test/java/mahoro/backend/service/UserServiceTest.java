package mahoro.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import mahoro.backend.model.Location;
import mahoro.backend.model.RoleType;
import mahoro.backend.model.User;
import mahoro.backend.repository.LocationRepository;
import mahoro.backend.repository.UserRepository;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private UserService userService;

    private User user;
    private Location location;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        location = new Location();
        location.setLocationId(UUID.randomUUID());
        location.setType(mahoro.backend.model.LocationType.VILLAGE);

        user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("maha@gmail.com");
        user.setFullName("Mahoro Darwin");
        user.setAssignedLocation(location);
        user.setLocationAssigned(true);
        user.setActive(false);
        user.setRole(RoleType.USER);
    }

    @Test
    void testCreateUserWithOtp() {
        String otp = userService.createUserWithOtp(user);
        assertThat(otp).hasSize(6);
    }

    @Test
    void testVerifyOtp_Success() {
        String otp = userService.createUserWithOtp(user);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        User verifiedUser = userService.verifyOtp(user.getEmail(), otp);

        assertThat(verifiedUser.isActive()).isTrue();
    }

    @Test
    void testVerifyOtp_InvalidOtp() {
        userService.createUserWithOtp(user);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () ->
            userService.verifyOtp(user.getEmail(), "wrongotp")
        );
    }

    @Test
    void testGetUserById_Success() {
        when(userRepository.findByUserId(user.getUserId())).thenReturn(Optional.of(user));

        User foundUser = userService.getUserById(user.getUserId());
        assertThat(foundUser.getEmail()).isEqualTo("maha@gmail.com");
    }

    @Test
    void testGetUserById_NotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findByUserId(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
            userService.getUserById(id)
        );
    }

    @Test
    void testGetAllUsers() {
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);

        Page<User> result = userService.getAllUsers(PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("maha@gmail.com");
    }

    @Test
    void testUpdateUser_EmailTaken() {
        UUID id = user.getUserId();
        User updatedDetails = new User();
        updatedDetails.setEmail("existing@example.com");
        updatedDetails.setFullName("New Name");
        updatedDetails.setRole(RoleType.ADMIN);

        when(userRepository.findByUserId(id)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
            userService.updateUser(id, updatedDetails, null)
        );
    }

    @Test
    void testDeleteUser_Success() {
        UUID id = user.getUserId();
        when(userRepository.existsById(id)).thenReturn(true);

        userService.deleteUser(id);
        verify(userRepository, times(1)).deleteById(id);
    }

    @Test
    void testDeleteUser_NotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.existsById(id)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
            userService.deleteUser(id)
        );
    }

    @Test
    void testFindUsersByLocation() {
        Page<User> page = new PageImpl<>(List.of(user));
        when(locationRepository.findById(location.getLocationId())).thenReturn(Optional.of(location));
        when(userRepository.findByAssignedLocation(location, PageRequest.of(0, 10))).thenReturn(page);

        Page<User> result = userService.findUsersByLocation(location.getLocationId(), PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
    }
}
