package mahoro.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID deviceId;

    @Column(nullable = false, unique = true)
    private String imei;

    @Column(unique = true)
    private String serialNumber;

    @Column(name = "last_reported_at")
    private LocalDateTime LastReportedAt;

    private String model;

    @Column(nullable = false)
    private LocalDateTime registrationDate = LocalDateTime.now();

   
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "assignedLocation", "devices"})
    @JsonIgnore
    private User primaryUser;


    @OneToMany(mappedBy = "device")
    @JsonManagedReference
    private Set<LocationHistory> locationHistory;

  
    @ManyToMany(mappedBy = "devices")
    private Set<DeviceGroup> groups;


    public UUID getDeviceId() {
        return deviceId;
    }


    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }


    public String getImei() {
        return imei;
    }


    public void setImei(String imei) {
        this.imei = imei;
    }


    public String getSerialNumber() {
        return serialNumber;
    }


    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }


    public String getModel() {
        return model;
    }


    public void setModel(String model) {
        this.model = model;
    }


    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }


    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }


    public User getPrimaryUser() {
        return primaryUser;
    }


    public void setPrimaryUser(User primaryUser) {
        this.primaryUser = primaryUser;
    }


    public Set<LocationHistory> getLocationHistory() {
        return locationHistory;
    }


    public void setLocationHistory(Set<LocationHistory> locationHistory) {
        this.locationHistory = locationHistory;
    }


    public Set<DeviceGroup> getGroups() {
        return groups;
    }


    public void setGroups(Set<DeviceGroup> groups) {
        this.groups = groups;
    }


    public LocalDateTime getLastReportedAt() {
        return LastReportedAt;
    }


    public void setLastReportedAt(LocalDateTime lastReportedAt) {
        LastReportedAt = lastReportedAt;
    }


 

    
    
}