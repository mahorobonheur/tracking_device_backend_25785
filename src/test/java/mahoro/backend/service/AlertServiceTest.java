package mahoro.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import mahoro.backend.model.AlertType;
import mahoro.backend.model.Device;
import mahoro.backend.model.TrackingAlert;
import mahoro.backend.repository.AlertRepository;
import mahoro.backend.repository.DeviceRepository;

class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @InjectMocks
    private AlertService alertService;

    private Device device;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        device = new Device();
        device.setDeviceId(UUID.randomUUID());
        device.setDeviceName("Device1");
    }

    @Test
    void testCreateAlertSuccess() {
        TrackingAlert savedAlert = new TrackingAlert();
        savedAlert.setDevice(device);
        savedAlert.setMessage("Test alert");
        savedAlert.setAlertType(AlertType.LOW_BATTERY);

        when(deviceRepository.findById(device.getDeviceId())).thenReturn(Optional.of(device));
        when(alertRepository.save(any(TrackingAlert.class))).thenReturn(savedAlert);

        TrackingAlert alert = alertService.createAlert(device.getDeviceId(), "Test alert", AlertType.LOW_BATTERY, null, null);

        assertNotNull(alert);
        assertEquals("Test alert", alert.getMessage());
        assertEquals(AlertType.LOW_BATTERY, alert.getAlertType());
    }

    @Test
    void testCreateAlertDeviceNotFound() {
        UUID deviceId = UUID.randomUUID();
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> alertService.createAlert(deviceId, "Test", AlertType.MOVEMENT_DETECTED, null, null));

        assertTrue(ex.getMessage().contains("Device not found"));
    }

    @Test
    void testMarkAlertAsResolved() {
        TrackingAlert alert = new TrackingAlert();
        alert.setId(UUID.randomUUID());
        alert.setResolved(false);

        when(alertRepository.findById(alert.getId())).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(TrackingAlert.class))).thenAnswer(i -> i.getArgument(0));

        TrackingAlert resolved = alertService.markAlertAsResolved(alert.getId());

        assertTrue(resolved.isResolved());
        assertNotNull(resolved.getResolvedAt());
    }

    @Test
    void testMarkAlertAsResolvedNotFound() {
        UUID alertId = UUID.randomUUID();
        when(alertRepository.findById(alertId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> alertService.markAlertAsResolved(alertId));

        assertTrue(ex.getMessage().contains("Alert not found"));
    }

    @Test
    void testGetAlertStatistics() {
        TrackingAlert a1 = new TrackingAlert();
        a1.setAlertType(AlertType.GEOFENCE_VIOLATION);
        a1.setResolved(false);
        TrackingAlert a2 = new TrackingAlert();
        a2.setAlertType(AlertType.LOW_BATTERY);
        a2.setResolved(true);
        TrackingAlert a3 = new TrackingAlert();
        a3.setAlertType(AlertType.MOVEMENT_DETECTED);
        a3.setResolved(false);

        when(alertRepository.findByDevice_User_UserId(device.getDeviceId()))
                .thenReturn(List.of(a1, a2, a3));

        Map<String, Long> stats = alertService.getAlertStatistics(device.getDeviceId());

        assertEquals(3, stats.get("total"));
        assertEquals(2, stats.get("unresolved"));
        assertEquals(1, stats.get("geofence"));
        assertEquals(1, stats.get("battery"));
        assertEquals(1, stats.get("movement"));
    }

    @Test
    void testGetActiveAlertsByDevice() {
        TrackingAlert alert = new TrackingAlert();
        alert.setResolved(false);

        when(alertRepository.findByDevice_DeviceIdAndResolvedFalseOrderByCreatedAtDesc(device.getDeviceId()))
                .thenReturn(List.of(alert));

        List<TrackingAlert> alerts = alertService.getActiveAlertsByDevice(device.getDeviceId());

        assertEquals(1, alerts.size());
        assertFalse(alerts.get(0).isResolved());
    }

    @Test
    void testDeleteAlert() {
        UUID alertId = UUID.randomUUID();
        doNothing().when(alertRepository).deleteById(alertId);

        assertDoesNotThrow(() -> alertService.deleteAlert(alertId));
        verify(alertRepository, times(1)).deleteById(alertId);
    }

    @Test
    void testSearchAlertsEmptyTerm() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<TrackingAlert> page = new PageImpl<>(List.of(new TrackingAlert()));

        when(alertRepository.findAll(pageable)).thenReturn(page);

        Page<TrackingAlert> result = alertService.searchAlerts("", pageable);

        assertEquals(1, result.getContent().size());
    }
}
