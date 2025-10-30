package mahoro.backend.repository;

import mahoro.backend.model.Location;
import mahoro.backend.model.User;
import mahoro.backend.model.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUserId(UUID userId);

    List<User> findAll();

    Optional<User> findByEmail(String email);

    Page<User> findByAssignedLocation(Location location, Pageable pageable);

    boolean existsByEmail(String email);

    @Query("SELECT p FROM User p JOIN p.roles r WHERE r = :role")
    Page<User> findByRole(@Param("role") RoleType role, Pageable pageable);

}