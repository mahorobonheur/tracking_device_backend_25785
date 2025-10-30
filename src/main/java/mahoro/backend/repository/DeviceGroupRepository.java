package mahoro.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mahoro.backend.model.DeviceGroup;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceGroupRepository extends JpaRepository<DeviceGroup, UUID> {
 
    boolean existsByName(String name);
    
    Optional<DeviceGroup> findByName(String name);
}