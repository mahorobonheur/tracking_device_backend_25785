package mahoro.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mahoro.backend.model.Location;
import mahoro.backend.model.LocationType;
import mahoro.backend.repository.LocationRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Transactional
    public Location saveLocation(Location location, UUID parentId) {
        if (parentId != null) {
            Optional<Location> parentOpt = locationRepository.findById(parentId);
            if (parentOpt.isEmpty()) {
                throw new IllegalArgumentException("Parent Location not found with ID: " + parentId);
            }
            Location parent = parentOpt.get();
            if (location.getType().ordinal() <= parent.getType().ordinal()) {
                 throw new IllegalArgumentException("Child location type must be lower in hierarchy than parent.");
            }
            location.setParent(parent);
        }
        return locationRepository.save(location);
    }


    public Page<Location> findAllLocations(Pageable pageable) {
        return locationRepository.findAll(pageable);
    }

  
    public List<Location> findChildrenOf(UUID parentId) {
        return locationRepository.findByParent_LocationId(parentId);
    }

    public Page<Location> findByType(LocationType type, Pageable pageable) {
        return locationRepository.findByType(type, pageable);
    }


    public Location findById(UUID id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with ID: " + id));
    }
}