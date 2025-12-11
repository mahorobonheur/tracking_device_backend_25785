package mahoro.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import lombok.Data;
import mahoro.backend.model.Assignment;
import mahoro.backend.model.Device;
import mahoro.backend.model.Location;
import mahoro.backend.model.TrackingAlert;
import mahoro.backend.model.User;

@Data
public class SearchResultDTO {
    private String query;
    private LocalDateTime timestamp;
    private Map<String, Long> summary;
    private Map<String, Long> quickSummary;
    
    // Full search results
    private Page<User> users;
    private Page<Device> devices;
    private Page<TrackingAlert> alerts;
    private Page<Location> locations;
    private Page<Assignment> assignments;
    
    // Quick search results
    private List<User> quickUsers;
    private List<Device> quickDevices;
    private List<TrackingAlert> quickAlerts;
    
    // Search metadata
    private boolean hasMoreResults;
    private int totalPages;
    private long totalElements;
}