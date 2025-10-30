package mahoro.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mahoro.backend.model.Device;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {

    Optional<Device> findByImei(String imei);

    Optional<Device> findBySerialNumber(String serialNumber);

    Page<Device> findByModelContainingIgnoreCase(String model, Pageable pageable);

    boolean existsByImei(String imei);

    boolean existsBySerialNumber(String serialNumber);
}