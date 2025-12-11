package mahoro.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mahoro.backend.model.TrackingAlert;

@Repository
public interface AlertRepository extends JpaRepository<TrackingAlert, UUID>, JpaSpecificationExecutor<TrackingAlert> {
 
    List<TrackingAlert> findByDevice_DeviceIdAndResolvedFalseOrderByCreatedAtDesc(UUID deviceId);
    
    List<TrackingAlert> findByDevice_User_UserIdAndResolvedFalseOrderByCreatedAtDesc(UUID userId);
    
    long countByDevice_User_UserIdAndResolvedFalse(UUID userId);
    
    List<TrackingAlert> findByDevice_User_UserId(UUID userId);
    
    @Query("SELECT a FROM TrackingAlert a WHERE a.device.user.userId = :userId " +
           "ORDER BY a.createdAt DESC")
    Page<TrackingAlert> findByUser(@Param("userId") UUID userId, Pageable pageable);
    
    Page<TrackingAlert> findAll(Pageable pageable);

    @Query("SELECT a FROM TrackingAlert a LEFT JOIN a.device d WHERE " +
           "LOWER(a.message) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(CAST(a.alertType AS string)) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "(d IS NOT NULL AND LOWER(d.imei) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<TrackingAlert> searchAlerts(@Param("query") String query, Pageable pageable);
}