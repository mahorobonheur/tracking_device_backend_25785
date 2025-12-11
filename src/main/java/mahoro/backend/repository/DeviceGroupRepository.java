package mahoro.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mahoro.backend.model.DeviceGroup;

@Repository
public interface DeviceGroupRepository extends JpaRepository<DeviceGroup, UUID> {
 
    boolean existsByName(String name);
    
    Optional<DeviceGroup> findByName(String name);
}