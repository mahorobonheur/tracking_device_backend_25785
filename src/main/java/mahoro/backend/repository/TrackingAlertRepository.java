 package mahoro.backend.repository;
import mahoro.backend.model.Device;
import mahoro.backend.model.TrackingAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TrackingAlertRepository extends JpaRepository<TrackingAlert, UUID> {
     List<TrackingAlert> findByDeviceAndCreatedAtAfter(Device device, LocalDateTime timestamp);
}
