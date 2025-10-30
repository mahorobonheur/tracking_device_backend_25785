package mahoro.backend.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mahoro.backend.model.Assignment;
import mahoro.backend.model.Device;
import mahoro.backend.model.User;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {

    List<Assignment> findByUserOrderByAssignmentDateDesc(User user);
    
    List<Assignment> findByUserAndReturnDateIsNull(User user);
    
    Assignment findByDeviceAndReturnDateIsNull(Device device);
}
