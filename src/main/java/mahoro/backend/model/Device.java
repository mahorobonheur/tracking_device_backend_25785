package mahoro.backend.model;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

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
    private LocalDateTime lastReportedAt;  
    
    private String model;

    @Column(nullable = false)
    private LocalDateTime registrationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceType deviceType = DeviceType.MOBILE;

    @Column
    private String deviceName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") 
    @JsonIgnoreProperties({"hibernateLazyInitializer", "assignedLocation", "devices"})
    private User user;

    @OneToMany(mappedBy = "device")
    @JsonManagedReference
    private Set<LocationHistory> locationHistory;

    @ManyToMany(mappedBy = "devices")
    @JsonIgnore
    private Set<DeviceGroup> groups;

    @PrePersist
    protected void onCreate() {
        registrationDate = LocalDateTime.now();
        lastReportedAt = LocalDateTime.now();
    }

    // Getters and setters
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
        return lastReportedAt;
    }

    public void setLastReportedAt(LocalDateTime lastReportedAt) {
        this.lastReportedAt = lastReportedAt;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}