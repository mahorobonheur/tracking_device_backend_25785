package mahoro.backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "location_history")
public class LocationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID locationHistoryId;

    private Double latitude;
    private Double longitude;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    private String recordedAddress;

   
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    @JsonBackReference
    private Device device;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    @JsonIgnore
    private User recordedBy;

    @Transient
    private List<TrackingAlert> alerts;  

    public List<TrackingAlert> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<TrackingAlert> alerts) {
        this.alerts = alerts;
    }


    public UUID getLocationHistoryId() {
        return locationHistoryId;
    }


    public void setLocationHistoryId(UUID locationHistoryId) {
        this.locationHistoryId = locationHistoryId;
    }


    public Double getLatitude() {
        return latitude;
    }


    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }


    public Double getLongitude() {
        return longitude;
    }


    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }


    public LocalDateTime getTimestamp() {
        return timestamp;
    }


    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }


    public String getRecordedAddress() {
        return recordedAddress;
    }


    public void setRecordedAddress(String recordedAddress) {
        this.recordedAddress = recordedAddress;
    }


    public Device getDevice() {
        return device;
    }


    public void setDevice(Device device) {
        this.device = device;
    }


    public User getRecordedBy() {
        return recordedBy;
    }


    public void setRecordedBy(User recordedBy) {
        this.recordedBy = recordedBy;
    }

    
}
