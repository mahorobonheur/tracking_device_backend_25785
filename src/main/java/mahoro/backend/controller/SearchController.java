package mahoro.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mahoro.backend.dto.SearchResultDTO;
import mahoro.backend.service.SearchService;

@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/global")
    public ResponseEntity<?> globalSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        try {
            log.info("Global search request: query='{}', page={}, size={}", query, page, size);
            
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Search query cannot be empty"));
            }
            
            // Create pageable without sorting for global search
            // Sorting will be handled separately for each entity type in SearchService
            Pageable pageable = PageRequest.of(page, size);
            
            // Perform search
            SearchResultDTO results = searchService.globalSearch(query.trim(), pageable);
            
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            log.error("Error during global search: ", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Search failed: " + e.getMessage()));
        }
    }

    @GetMapping("/quick")
    public ResponseEntity<?> quickSearch(@RequestParam String query) {
        try {
            log.info("Quick search request: query='{}'", query);
            
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Search query cannot be empty"));
            }
            
            // Perform quick search (limited to 5 results per category)
            SearchResultDTO results = searchService.quickSearch(query.trim(), 5);
            
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            log.error("Error during quick search: ", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Quick search failed: " + e.getMessage()));
        }
    }

    @GetMapping("/suggestions")
    public ResponseEntity<?> getSearchSuggestions(@RequestParam String prefix) {
        try {
            log.info("Getting search suggestions for prefix: '{}'", prefix);
            
            if (prefix == null || prefix.trim().isEmpty() || prefix.trim().length() < 2) {
                return ResponseEntity.ok(Map.of(
                    "users", new String[0],
                    "devices", new String[0],
                    "locations", new String[0]
                ));
            }
            
            Map<String, Object> suggestions = searchService.getSearchSuggestions(prefix.trim());
            
            return ResponseEntity.ok(suggestions);
            
        } catch (Exception e) {
            log.error("Error getting search suggestions: ", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to get suggestions: " + e.getMessage()));
        }
    }

    @GetMapping("/advanced")
    public ResponseEntity<?> advancedSearch(
            @RequestParam(required = false) String userQuery,
            @RequestParam(required = false) String deviceQuery,
            @RequestParam(required = false) String alertQuery,
            @RequestParam(required = false) String locationQuery,
            @RequestParam(required = false) String assignmentQuery,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            log.info("Advanced search request - user: {}, device: {}, alert: {}, location: {}, assignment: {}",
                    userQuery, deviceQuery, alertQuery, locationQuery, assignmentQuery);
            
            // Build combined query for simplicity
            StringBuilder combinedQuery = new StringBuilder();
            if (userQuery != null && !userQuery.trim().isEmpty()) combinedQuery.append("user:").append(userQuery).append(" ");
            if (deviceQuery != null && !deviceQuery.trim().isEmpty()) combinedQuery.append("device:").append(deviceQuery).append(" ");
            if (alertQuery != null && !alertQuery.trim().isEmpty()) combinedQuery.append("alert:").append(alertQuery).append(" ");
            if (locationQuery != null && !locationQuery.trim().isEmpty()) combinedQuery.append("location:").append(locationQuery).append(" ");
            if (assignmentQuery != null && !assignmentQuery.trim().isEmpty()) combinedQuery.append("assignment:").append(assignmentQuery).append(" ");
            
            if (combinedQuery.length() == 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "At least one search field must be provided"));
            }
            
            Pageable pageable = PageRequest.of(page, size);
            SearchResultDTO results = searchService.globalSearch(combinedQuery.toString().trim(), pageable);
            
            // Filter results based on specific queries
            Map<String, Object> response = new HashMap<>();
            response.put("query", combinedQuery.toString());
            response.put("results", results);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error during advanced search: ", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Advanced search failed: " + e.getMessage()));
        }
    }
}