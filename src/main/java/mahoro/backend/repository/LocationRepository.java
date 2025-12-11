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

import mahoro.backend.dto.LocationDTO;
import mahoro.backend.model.Location;
import mahoro.backend.model.LocationType;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID>, JpaSpecificationExecutor<Location>{

    Optional<Location> findByNameAndType(String name, LocationType type);

    List<Location> findByParent_LocationId(UUID parentId);

    List<Location> findByType(LocationType type);

    Page<Location> findByType(LocationType type, Pageable pageable);

    boolean existsByNameAndType(String name, LocationType type);
    
    @Query("SELECT new mahoro.backend.dto.LocationDTO(l.locationId, l.name, l.type, l.centerLatitude, l.centerLongitude, l.parent.locationId) FROM Location l WHERE l.parent.locationId = :parentId")
    List<LocationDTO> findChildrenDTOs(@Param("parentId") UUID parentId);

    @Query("SELECT l FROM Location l LEFT JOIN l.parent p WHERE " +
           "LOWER(l.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(CAST(l.type AS string)) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "(p IS NOT NULL AND LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Location> searchLocations(@Param("query") String query, Pageable pageable);
}