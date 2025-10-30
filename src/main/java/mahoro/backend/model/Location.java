package mahoro.backend.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID locationId;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LocationType type;

    @Column(name = "center_latitude")
    private Double centerLatitude;

    @Column(name = "center_longitude")
    private Double centerLongitude;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Location parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Location> children = new HashSet<>();


    public UUID getLocationId() {
        return locationId;
    }


    public void setLocationId(UUID locationId) {
        this.locationId = locationId;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public LocationType getType() {
        return type;
    }


    public void setType(LocationType type) {
        this.type = type;
    }


    public Location getParent() {
        return parent;
    }


    public void setParent(Location parent) {
        this.parent = parent;
    }


    public Double getCenterLatitude() {
        return centerLatitude;
    }

    public void setCenterLatitude(Double centerLatitude) {
        this.centerLatitude = centerLatitude;
    }


    public Double getCenterLongitude() {
        return centerLongitude;
    }


    public void setCenterLongitude(Double centerLongitude) {
        this.centerLongitude = centerLongitude;
    }


    public Set<Location> getChildren() {
        return children;
    }


    public void setChildren(Set<Location> children) {
        this.children = children;
    }


    

    
}