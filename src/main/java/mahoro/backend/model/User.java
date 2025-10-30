package mahoro.backend.model;

import jakarta.persistence.*;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; 

    @Column(nullable = false)
    private boolean active = false; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Location assignedLocation;

    @OneToOne(mappedBy = "primaryUser", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Device primaryDevice; 

    @ElementCollection(targetClass = RoleType.class)
    @CollectionTable(name = "person_roles", joinColumns = @JoinColumn(name = "person_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role_type")
    private Set<RoleType> roles = new HashSet<>();

    @ManyToMany(mappedBy = "members")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "locationHistory", "groups"})
    private Set<DeviceGroup> groups = new HashSet<>();


    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Location getAssignedLocation() {
        return assignedLocation;
    }

    public void setAssignedLocation(Location assignedLocation) {
        this.assignedLocation = assignedLocation;
    }

    public Device getPrimaryDevice() {
        return primaryDevice;
    }

    public void setPrimaryDevice(Device primaryDevice) {
        this.primaryDevice = primaryDevice;
    }

    public Set<RoleType> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleType> roles) {
        this.roles = roles;
    }

    public Set<DeviceGroup> getGroups() {
        return groups;
    }

    public void setGroups(Set<DeviceGroup> groups) {
        this.groups = groups;
    }
}
