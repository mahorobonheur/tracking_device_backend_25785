package mahoro.backend.dto;

import java.util.UUID;

public record ProfileUserDTO(
    UUID userId,
    String email, 
    String fullName
    
) {}