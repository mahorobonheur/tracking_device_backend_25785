package mahoro.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mahoro.backend.dto.SearchResultDTO;
import mahoro.backend.model.Assignment;
import mahoro.backend.model.Device;
import mahoro.backend.model.Location;
import mahoro.backend.model.TrackingAlert;
import mahoro.backend.model.User;
import mahoro.backend.repository.AlertRepository;
import mahoro.backend.repository.AssignmentRepository;
import mahoro.backend.repository.DeviceRepository;
import mahoro.backend.repository.LocationHistoryRepository;
import mahoro.backend.repository.LocationRepository;
import mahoro.backend.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final AlertRepository alertRepository;
    private final AssignmentRepository assignmentRepository;
    private final LocationRepository locationRepository;
    private final LocationHistoryRepository locationHistoryRepository;

    @Transactional(readOnly = true)
    public SearchResultDTO globalSearch(String query, Pageable pageable) {
        log.info("Performing global search for query: '{}'", query);
        
        SearchResultDTO result = new SearchResultDTO();
        result.setQuery(query);
        result.setTimestamp(LocalDateTime.now());
        
        try {
            // Create pageable without sorting for each search to avoid property reference errors
            Pageable unsortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
            
            // Search users - use default sorting by fullName
            Page<User> users = searchUsers(query, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), 
                org.springframework.data.domain.Sort.by("fullName")));
            result.setUsers(users);
            
            // Search devices - use default sorting by deviceName
            Page<Device> devices = searchDevices(query, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                org.springframework.data.domain.Sort.by("deviceName")));
            result.setDevices(devices);
            
            // Search alerts - use default sorting by timestamp (if exists) or message
            Page<TrackingAlert> alerts = searchAlerts(query, unsortedPageable);
            result.setAlerts(alerts);
            
            // Search locations - use default sorting by name
            Page<Location> locations = searchLocations(query, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                org.springframework.data.domain.Sort.by("name")));
            result.setLocations(locations);
            
            // Search assignments - use default sorting by assignment date or reason
            Page<Assignment> assignments = searchAssignments(query, unsortedPageable);
            result.setAssignments(assignments);
            
            // Set summary statistics
            Map<String, Long> summary = new HashMap<>();
            summary.put("users", users.getTotalElements());
            summary.put("devices", devices.getTotalElements());
            summary.put("alerts", alerts.getTotalElements());
            summary.put("locations", locations.getTotalElements());
            summary.put("assignments", assignments.getTotalElements());
            summary.put("total", users.getTotalElements() + devices.getTotalElements() + 
                         alerts.getTotalElements() + locations.getTotalElements() + 
                         assignments.getTotalElements());
            
            result.setSummary(summary);
            
            log.info("Global search completed. Found {} total results", summary.get("total"));
            
        } catch (Exception e) {
            log.error("Error during global search: ", e);
            throw new RuntimeException("Search failed: " + e.getMessage());
        }
        
        return result;
    }

    private Page<User> searchUsers(String query, Pageable pageable) {
        String searchTerm = "%" + query.toLowerCase() + "%";
        
        return userRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            
            predicates.add(criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchTerm),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), searchTerm),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("role").as(String.class)), searchTerm)
            ));
            
            // Search in assigned location name if exists
            try {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("assignedLocation").get("name")), 
                    searchTerm
                ));
            } catch (Exception ignored) {
                // defensive: assignedLocation might be null in metamodel path resolution
            }
            
            return criteriaBuilder.or(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, pageable);
    }

    private Page<Device> searchDevices(String query, Pageable pageable) {
        String searchTerm = "%" + query.toLowerCase() + "%";
        
        return deviceRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            
            // Search in device fields
            predicates.add(criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("imei")), searchTerm),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("serialNumber")), searchTerm),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("model")), searchTerm),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("deviceName")), searchTerm),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("deviceType").as(String.class)), searchTerm)
            ));
            
            // Search in user fields if device has a user
            try {
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("email")), searchTerm),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("fullName")), searchTerm)
                ));
            } catch (Exception ignored) {
                // defensive: user path might not exist
            }
            
            // Try to parse as date for registration date search
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                // parse into LocalDate, then convert to start of day LocalDateTime
                LocalDate parsed = LocalDate.parse(query, formatter);
                LocalDateTime searchDate = parsed.atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("registrationDate"), searchDate
                ));
            } catch (DateTimeParseException e) {
                // Not a date, continue
            } catch (Exception e) {
                // safe-guard if path types differ
            }
            
            return criteriaBuilder.or(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, pageable);
    }

    private Page<TrackingAlert> searchAlerts(String query, Pageable pageable) {
        String searchTerm = "%" + query.toLowerCase() + "%";
        
        return alertRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            
            predicates.add(criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("message")), searchTerm),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("alertType").as(String.class)), searchTerm)
            ));
            
            // Search in device fields if alert has a device
            try {
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("device").get("imei")), searchTerm),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("device").get("model")), searchTerm)
                ));
            } catch (Exception ignored) {}
            
            return criteriaBuilder.or(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, pageable);
    }

    private Page<Location> searchLocations(String query, Pageable pageable) {
        String searchTerm = "%" + query.toLowerCase() + "%";
        
        return locationRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            
            predicates.add(criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchTerm),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("type").as(String.class)), searchTerm)
            ));
            
            // Search in parent location name if exists
            try {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("parent").get("name")), 
                    searchTerm
                ));
            } catch (Exception ignored) {}
            
            return criteriaBuilder.or(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, pageable);
    }

    private Page<Assignment> searchAssignments(String query, Pageable pageable) {
        String searchTerm = "%" + query.toLowerCase() + "%";
        
        return assignmentRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("reason")), searchTerm));
            
            // Search in user fields
            try {
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("email")), searchTerm),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("fullName")), searchTerm)
                ));
            } catch (Exception ignored) {}
            
            // Search in device fields
            try {
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("device").get("imei")), searchTerm),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("device").get("model")), searchTerm),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("device").get("deviceName")), searchTerm)
                ));
            } catch (Exception ignored) {}
            
            return criteriaBuilder.or(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, pageable);
    }

    @Transactional(readOnly = true)
    public SearchResultDTO quickSearch(String query, int limit) {
        log.info("Performing quick search for query: '{}' with limit: {}", query, limit);
        
        SearchResultDTO result = new SearchResultDTO();
        result.setQuery(query);
        result.setTimestamp(LocalDateTime.now());
        
        try {
            // Quick search for users (limited results)
            List<User> users = userRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
                String searchTerm = "%" + query.toLowerCase() + "%";
                return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchTerm),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), searchTerm)
                );
            }).stream().limit(limit).collect(Collectors.toList());
            result.setQuickUsers(users);
            
            // Quick search for devices (limited results)
            List<Device> devices = deviceRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
                String searchTerm = "%" + query.toLowerCase() + "%";
                return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("imei")), searchTerm),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("model")), searchTerm),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("deviceName")), searchTerm)
                );
            }).stream().limit(limit).collect(Collectors.toList());
            result.setQuickDevices(devices);
            
            // Quick search for alerts (limited results)
            List<TrackingAlert> alerts = alertRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
                String searchTerm = "%" + query.toLowerCase() + "%";
                return criteriaBuilder.like(criteriaBuilder.lower(root.get("message")), searchTerm);
            }).stream().limit(limit).collect(Collectors.toList());
            result.setQuickAlerts(alerts);
            
            // Set quick summary
            Map<String, Long> quickSummary = new HashMap<>();
            quickSummary.put("users", (long) users.size());
            quickSummary.put("devices", (long) devices.size());
            quickSummary.put("alerts", (long) alerts.size());
            quickSummary.put("total", (long) (users.size() + devices.size() + alerts.size()));
            
            result.setQuickSummary(quickSummary);
            
            log.info("Quick search completed. Found {} total results", quickSummary.get("total"));
            
        } catch (Exception e) {
            log.error("Error during quick search: ", e);
            throw new RuntimeException("Quick search failed: " + e.getMessage());
        }
        
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSearchSuggestions(String prefix) {
        Map<String, Object> suggestions = new HashMap<>();
        
        if (prefix.length() < 2) {
            return suggestions;
        }
        
        try {
            // Get user email suggestions
            List<String> userEmails = userRepository.findAll()
                .stream()
                .map(User::getEmail)
                .filter(email -> email.toLowerCase().startsWith(prefix.toLowerCase()))
                .limit(5)
                .collect(Collectors.toList());
            suggestions.put("users", userEmails);
            
            // Get device IMEI suggestions
            List<String> deviceImeis = deviceRepository.findAll()
                .stream()
                .map(Device::getImei)
                .filter(imei -> imei != null && imei.toLowerCase().startsWith(prefix.toLowerCase()))
                .limit(5)
                .collect(Collectors.toList());
            suggestions.put("devices", deviceImeis);
            
            // Get location name suggestions
            List<String> locationNames = locationRepository.findAll()
                .stream()
                .map(Location::getName)
                .filter(name -> name.toLowerCase().startsWith(prefix.toLowerCase()))
                .limit(5)
                .collect(Collectors.toList());
            suggestions.put("locations", locationNames);
            
        } catch (Exception e) {
            log.error("Error getting search suggestions: ", e);
        }
        
        return suggestions;
    }
}