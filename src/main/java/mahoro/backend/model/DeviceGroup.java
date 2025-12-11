package mahoro.backend.model;



import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

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
    
}