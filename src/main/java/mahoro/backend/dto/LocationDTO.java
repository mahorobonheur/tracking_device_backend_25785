package mahoro.backend.dto;

import java.util.UUID;

import mahoro.backend.model.LocationType;

public record LocationDTO(
    UUID locationId,
    String name,
    LocationType type,
    Double centerLatitude,
    Double centerLongitude,
    UUID parentId
) {}