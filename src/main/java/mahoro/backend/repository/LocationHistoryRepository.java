package mahoro.backend.repository;

// package mahoro.backend.repository;
import mahoro.backend.model.Device;
import mahoro.backend.model.LocationHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LocationHistoryRepository extends JpaRepository<LocationHistory, UUID> {

    @Query("SELECT lh FROM LocationHistory lh WHERE lh.device = ?1 ORDER BY lh.timestamp DESC")
    List<LocationHistory> findLatestByDevice(Device device, Pageable pageable);
  
    List<LocationHistory> findByDeviceAndTimestampBetweenOrderByTimestampAsc(
            Device device, LocalDateTime from, LocalDateTime to);

    List<LocationHistory> findByDeviceOrderByTimestampAsc(Device device);
}