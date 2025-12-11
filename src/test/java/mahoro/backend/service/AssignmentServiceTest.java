package mahoro.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import mahoro.backend.model.Assignment;
import mahoro.backend.model.Device;
import mahoro.backend.model.User;
import mahoro.backend.repository.AssignmentRepository;
import mahoro.backend.repository.DeviceRepository;
import mahoro.backend.repository.UserRepository;

class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @InjectMocks
    private AssignmentService assignmentService;

    private User user;
    private Device device;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setUserId(UUID.randomUUID());
        user.setFullName("John Doe");

        device = new Device();
        device.setDeviceId(UUID.randomUUID());
        device.setDeviceName("Device 1");
    }

    @Test
    void testAssignDeviceSuccess() {
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(deviceRepository.findById(device.getDeviceId())).thenReturn(Optional.of(device));
        when(assignmentRepository.findByDeviceAndReturnDateIsNull(device)).thenReturn(null);
        when(assignmentRepository.findByUserAndReturnDateIsNull(user)).thenReturn(Collections.emptyList());
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(i -> i.getArgument(0));

        Assignment assignment = assignmentService.assignDevice(user.getUserId(), device.getDeviceId(), "For testing");

        assertNotNull(assignment);
        assertEquals(user, assignment.getUser());
        assertEquals(device, assignment.getDevice());
        assertEquals("For testing", assignment.getReason());
        assertNotNull(assignment.getAssignmentDate());
    }

    @Test
    void testAssignDeviceUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> assignmentService.assignDevice(userId, device.getDeviceId(), "Test"));

        assertTrue(ex.getMessage().contains("User not found"));
    }

    @Test
    void testAssignDeviceDeviceNotFound() {
        UUID deviceId = UUID.randomUUID();
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> assignmentService.assignDevice(user.getUserId(), deviceId, "Test"));

        assertTrue(ex.getMessage().contains("Device not found"));
    }

    @Test
    void testAssignDeviceAlreadyAssigned() {
        Assignment existingAssignment = new Assignment();
        existingAssignment.setUser(user);
        existingAssignment.setDevice(device);

        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(deviceRepository.findById(device.getDeviceId())).thenReturn(Optional.of(device));
        when(assignmentRepository.findByDeviceAndReturnDateIsNull(device)).thenReturn(existingAssignment);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> assignmentService.assignDevice(user.getUserId(), device.getDeviceId(), "Test"));

        assertTrue(ex.getMessage().contains("currently assigned"));
    }

    @Test
    void testReturnDeviceSuccess() {
        Assignment assignment = new Assignment();
        assignment.setDevice(device);
        assignment.setUser(user);
        assignment.setAssignmentDate(LocalDateTime.now());

        when(deviceRepository.findById(device.getDeviceId())).thenReturn(Optional.of(device));
        when(assignmentRepository.findByDeviceAndReturnDateIsNull(device)).thenReturn(assignment);
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(i -> i.getArgument(0));

        Assignment returned = assignmentService.returnDevice(device.getDeviceId());

        assertNotNull(returned.getReturnDate());
        assertEquals(user, returned.getUser());
    }

    @Test
    void testReturnDeviceNotAssigned() {
        when(deviceRepository.findById(device.getDeviceId())).thenReturn(Optional.of(device));
        when(assignmentRepository.findByDeviceAndReturnDateIsNull(device)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> assignmentService.returnDevice(device.getDeviceId()));

        assertTrue(ex.getMessage().contains("not currently assigned"));
    }

    @Test
    void testFindCurrentAssignmentsByPersonSuccess() {
        Assignment assignment = new Assignment();
        assignment.setDevice(device);
        assignment.setUser(user);

        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(assignmentRepository.findByUserAndReturnDateIsNull(user)).thenReturn(List.of(assignment));

        List<Assignment> result = assignmentService.findCurrentAssignmentsByPerson(user.getUserId());

        assertEquals(1, result.size());
        assertEquals(device, result.get(0).getDevice());
    }

    @Test
    void testFindCurrentAssignmentsByPersonUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> assignmentService.findCurrentAssignmentsByPerson(userId));

        assertTrue(ex.getMessage().contains("User not found"));
    }

    @Test
    void testFindAllActiveAssignments() {
        Assignment a1 = new Assignment();
        a1.setReturnDate(null);
        Assignment a2 = new Assignment();
        a2.setReturnDate(LocalDateTime.now());

        when(assignmentRepository.findAll()).thenReturn(List.of(a1, a2));

        List<Assignment> result = assignmentService.findAllActiveAssignments();
        assertEquals(1, result.size());
        assertNull(result.get(0).getReturnDate());
    }

    @Test
    void testFindAllAssignments() {
        Assignment a1 = new Assignment();
        Assignment a2 = new Assignment();

        when(assignmentRepository.findAll()).thenReturn(List.of(a1, a2));

        List<Assignment> result = assignmentService.findAllAssignments();
        assertEquals(2, result.size());
    }
}
