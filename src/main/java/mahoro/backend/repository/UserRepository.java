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

import mahoro.backend.model.Location;
import mahoro.backend.model.RoleType;
import mahoro.backend.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByUserId(UUID userId);

    List<User> findAll();

    Optional<User> findByEmail(String email);

    Page<User> findByAssignedLocation(Location location, Pageable pageable);

    boolean existsByEmail(String email);

    // Fix this method - it should return Optional<User>
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findProfileByEmail(@Param("email") String email); 

    @Query("SELECT u FROM User u WHERE u.role = :role")
    Page<User> findByRole(@Param("role") RoleType role, Pageable pageable);

     @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.role) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<User> searchUsers(@Param("query") String query, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> quickSearchUsers(@Param("query") String query, @Param("limit") int limit);
}