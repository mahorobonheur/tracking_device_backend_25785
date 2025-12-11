package mahoro.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mahoro.backend.model.Assignment;
import mahoro.backend.model.Device;
import mahoro.backend.model.User;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, UUID>, JpaSpecificationExecutor<Assignment> {

    List<Assignment> findByUserOrderByAssignmentDateDesc(User user);
    
    List<Assignment> findByUserAndReturnDateIsNull(User user);
    
    Assignment findByDeviceAndReturnDateIsNull(Device device);
    
    @Query("SELECT a FROM Assignment a WHERE a.returnDate IS NULL")
    List<Assignment> findActiveAssignments();

    @Query("SELECT a FROM Assignment a LEFT JOIN a.user u LEFT JOIN a.device d WHERE " +
           "LOWER(a.reason) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "(u IS NOT NULL AND LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(u IS NOT NULL AND LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(d IS NOT NULL AND LOWER(d.imei) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(d IS NOT NULL AND LOWER(d.model) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Assignment> searchAssignments(@Param("query") String query, Pageable pageable);
}