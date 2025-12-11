package mahoro.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mahoro.backend.model.Device;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID>, JpaSpecificationExecutor<Device> {
    
    Optional<Device> findByImei(String imei);
    
    Optional<Device> findBySerialNumber(String serialNumber);
    
    boolean existsByImei(String imei);
    
    boolean existsBySerialNumber(String serialNumber);
    
    Page<Device> findByUser_UserId(UUID userId, Pageable pageable);
    
    List<Device> findByUser_UserId(UUID userId);
    
    @Query("SELECT d FROM Device d LEFT JOIN d.user u WHERE " +
           "LOWER(d.model) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(d.imei) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(d.serialNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(d.deviceName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(CAST(d.deviceType AS string)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "(u IS NOT NULL AND LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) OR " +
           "CAST(d.registrationDate AS string) LIKE CONCAT('%', :searchTerm, '%')")
    Page<Device> searchDevices(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT COUNT(d) FROM Device d WHERE d.user.userId = :userId")
    long countByUserId(@Param("userId") UUID userId);
}