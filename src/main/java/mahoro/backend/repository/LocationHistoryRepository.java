package mahoro.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mahoro.backend.model.LocationHistory;

@Repository
public interface LocationHistoryRepository extends JpaRepository<LocationHistory, UUID> {
    
    Optional<LocationHistory> findFirstByDevice_DeviceIdOrderByTimestampDesc(UUID deviceId);
    
    List<LocationHistory> findByDevice_DeviceIdAndTimestampBetweenOrderByTimestampDesc(
        UUID deviceId, LocalDateTime start, LocalDateTime end);
    
    List<LocationHistory> findByDevice_DeviceIdAndTimestampBeforeOrderByTimestampDesc(
        UUID deviceId, LocalDateTime end);
    
    @Query("SELECT lh FROM LocationHistory lh " +
           "WHERE lh.device.user.userId = :userId " +
           "ORDER BY lh.timestamp DESC LIMIT :limit")
    List<LocationHistory> findRecentLocationsByUser(
        @Param("userId") UUID userId, 
        @Param("limit") int limit);
    
    @Query("SELECT lh FROM LocationHistory lh " +
           "WHERE lh.device.user.userId = :userId " +
           "AND lh.timestamp >= :startDate " +
           "ORDER BY lh.timestamp DESC")
    Page<LocationHistory> findByUserAndDateRange(
        @Param("userId") UUID userId,
        @Param("startDate") LocalDateTime startDate,
        Pageable pageable);

          List<LocationHistory> findByDevice_DeviceIdOrderByTimestampDesc(UUID deviceId);
    
    Optional<LocationHistory> findTopByDevice_DeviceIdOrderByTimestampDesc(UUID deviceId);
    
    List<LocationHistory> findByDevice_DeviceIdAndTimestampAfterOrderByTimestampDesc(
            UUID deviceId, LocalDateTime timestamp);
    
    @Query("SELECT l FROM LocationHistory l WHERE l.device.deviceId = :deviceId " +
           "AND l.timestamp >= :startDate AND l.timestamp <= :endDate " +
           "ORDER BY l.timestamp DESC")
    List<LocationHistory> findLocationHistoryBetweenDates(
            @Param("deviceId") UUID deviceId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}