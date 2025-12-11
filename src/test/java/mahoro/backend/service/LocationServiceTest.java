package mahoro.backend.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import mahoro.backend.model.Location;
import mahoro.backend.model.LocationType;
import mahoro.backend.repository.LocationRepository;

class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private LocationService locationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveLocationWithParent() {
        UUID parentId = UUID.randomUUID();
        Location parent = new Location();
        parent.setLocationId(parentId);
        parent.setType(LocationType.DISTRICT);

        Location child = new Location();
        child.setType(LocationType.SECTOR);

        when(locationRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(locationRepository.save(any(Location.class))).thenAnswer(i -> i.getArgument(0));

        Location saved = locationService.saveLocation(child, parentId);

        assertNotNull(saved);
        assertEquals(parent, saved.getParent());
    }

    @Test
    void testSaveLocationWithInvalidParentThrows() {
        UUID parentId = UUID.randomUUID();
        Location child = new Location();
        child.setType(LocationType.DISTRICT); // same as parent

        when(locationRepository.findById(parentId)).thenReturn(Optional.of(new Location(){{
            setType(LocationType.DISTRICT);
        }}));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
            locationService.saveLocation(child, parentId)
        );

        assertEquals("Child location type must be lower in hierarchy than parent.", ex.getMessage());
    }

    @Test
    void testFindByIdFound() {
        UUID id = UUID.randomUUID();
        Location location = new Location();
        location.setLocationId(id);

        when(locationRepository.findById(id)).thenReturn(Optional.of(location));

        Location result = locationService.findById(id);
        assertEquals(location, result);
    }

    @Test
    void testFindByIdNotFoundThrows() {
        UUID id = UUID.randomUUID();
        when(locationRepository.findById(id)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
            locationService.findById(id)
        );

        assertEquals("Location not found with ID: " + id, ex.getMessage());
    }

    @Test
    void testFindChildrenOf() {
        UUID parentId = UUID.randomUUID();
        Location child1 = new Location();
        Location child2 = new Location();

        when(locationRepository.findByParent_LocationId(parentId)).thenReturn(List.of(child1, child2));

        List<Location> children = locationService.findChildrenOf(parentId);
        assertEquals(2, children.size());
    }

    @Test
    void testUpdateLocation() {
        UUID id = UUID.randomUUID();
        Location existing = new Location();
        existing.setLocationId(id);
        existing.setName("OldName");

        Location details = new Location();
        details.setName("NewName");
        details.setType(LocationType.VILLAGE);

        when(locationRepository.findById(id)).thenReturn(Optional.of(existing));
        when(locationRepository.save(any(Location.class))).thenAnswer(i -> i.getArgument(0));

        Location updated = locationService.updateLocation(id, details);
        assertEquals("NewName", updated.getName());
        assertEquals(LocationType.VILLAGE, updated.getType());
    }

    @Test
    void testDeleteLocation() {
        UUID id = UUID.randomUUID();
        Location location = new Location();
        location.setLocationId(id);

        when(locationRepository.findById(id)).thenReturn(Optional.of(location));
        doNothing().when(locationRepository).delete(location);

        assertDoesNotThrow(() -> locationService.deleteLocation(id));
        verify(locationRepository, times(1)).delete(location);
    }
}
