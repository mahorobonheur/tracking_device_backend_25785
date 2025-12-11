package mahoro.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import mahoro.backend.model.Device;
import mahoro.backend.model.User;
import mahoro.backend.repository.DeviceRepository;
import mahoro.backend.repository.UserRepository;

class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeviceService deviceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateDeviceSuccess() {
        Device device = new Device();
        device.setImei("12345");

        when(deviceRepository.findByImei("12345")).thenReturn(Optional.empty());
        when(deviceRepository.findBySerialNumber(null)).thenReturn(Optional.empty());
        when(deviceRepository.save(any(Device.class))).thenAnswer(i -> i.getArgument(0));

        Device saved = deviceService.createDevice(device);

        assertNotNull(saved.getRegistrationDate());
        assertEquals("12345", saved.getImei());
        verify(deviceRepository, times(1)).save(device);
    }

    @Test
    void testCreateDeviceDuplicateImeiThrows() {
        Device device = new Device();
        device.setImei("12345");

        when(deviceRepository.findByImei("12345")).thenReturn(Optional.of(new Device()));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> deviceService.createDevice(device));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void testGetDeviceByIdFound() {
        UUID id = UUID.randomUUID();
        Device device = new Device();
        device.setDeviceId(id);

        when(deviceRepository.findById(id)).thenReturn(Optional.of(device));

        Device result = deviceService.getDeviceById(id);
        assertEquals(device, result);
    }

    @Test
    void testGetDeviceByIdNotFoundThrows() {
        UUID id = UUID.randomUUID();
        when(deviceRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> deviceService.getDeviceById(id));
        assertTrue(ex.getMessage().contains("Device not found"));
    }

    @Test
    void testUpdateDeviceChangesFields() {
        UUID id = UUID.randomUUID();
        Device existing = new Device();
        existing.setDeviceId(id);
        existing.setImei("12345");

        Device details = new Device();
        details.setDeviceName("NewName");
        details.setImei("67890");

        when(deviceRepository.findById(id)).thenReturn(Optional.of(existing));
        when(deviceRepository.existsByImei("67890")).thenReturn(false);
        when(deviceRepository.save(any(Device.class))).thenAnswer(i -> i.getArgument(0));

        Device updated = deviceService.updateDevice(id, details);
        assertEquals("NewName", updated.getDeviceName());
        assertEquals("67890", updated.getImei());
    }

    @Test
    void testDeleteDeviceSuccess() {
        UUID id = UUID.randomUUID();
        when(deviceRepository.existsById(id)).thenReturn(true);
        doNothing().when(deviceRepository).deleteById(id);

        assertDoesNotThrow(() -> deviceService.deleteDevice(id));
        verify(deviceRepository, times(1)).deleteById(id);
    }

    @Test
    void testDeleteDeviceNotFoundThrows() {
        UUID id = UUID.randomUUID();
        when(deviceRepository.existsById(id)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> deviceService.deleteDevice(id));
        assertTrue(ex.getMessage().contains("Device not found"));
    }

    @Test
    void testGetDevicesWithRecentActivitySorted() {
        UUID userId = UUID.randomUUID();
        Device d1 = new Device();
        d1.setLastReportedAt(LocalDateTime.now().minusMinutes(1));
        Device d2 = new Device();
        d2.setLastReportedAt(LocalDateTime.now().minusMinutes(10));

        when(deviceRepository.findByUser_UserId(userId)).thenReturn(List.of(d1, d2));

        List<Device> result = deviceService.getDevicesWithRecentActivity(userId);
        assertEquals(d1, result.get(0));
        assertEquals(d2, result.get(1));
    }

    @Test
    void testCountByUserId() {
        UUID userId = UUID.randomUUID();
        when(deviceRepository.countByUserId(userId)).thenReturn(5L);

        long count = deviceService.countByUserId(userId);
        assertEquals(5L, count);
    }
}
