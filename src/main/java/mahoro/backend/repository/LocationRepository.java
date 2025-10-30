package mahoro.backend.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mahoro.backend.model.Location;
import mahoro.backend.model.LocationType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

    Optional<Location> findByNameAndType(String name, LocationType type);

    List<Location> findByParent_LocationId(UUID parentId);

    Page<Location> findByType(LocationType type, Pageable pageable);

    boolean existsByNameAndType(String name, LocationType type);
}
