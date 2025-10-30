package mahoro.backend.model;



import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "device_groups")
public class DeviceGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID deviceGroupId;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    
    @ManyToMany
    @JoinTable(
        name = "group_devices",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "device_id")
    )
    private Set<Device> devices = new HashSet<>();

   
    @ManyToMany
    @JoinTable(
        name = "group_members",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "person_id")
    )
  
    private Set<User> members = new HashSet<>();


    public UUID getDeviceGroupId() {
        return deviceGroupId;
    }


    public void setDeviceGroupId(UUID deviceGroupId) {
        this.deviceGroupId = deviceGroupId;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public Set<Device> getDevices() {
        return devices;
    }


    public void setDevices(Set<Device> devices) {
        this.devices = devices;
    }


    public Set<User> getMembers() {
        return members;
    }


    public void setMembers(Set<User> members) {
        this.members = members;
    }

    
}