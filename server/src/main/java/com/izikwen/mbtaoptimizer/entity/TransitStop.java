package com.izikwen.mbtaoptimizer.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "transit_stops")
public class TransitStop {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, length = 100)
    private String mbtaStopId;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(nullable = false, length = 50)
    private String mode;
    @Column(nullable = false)
    private Double lat;
    @Column(nullable = false)
    private Double lng;
    @Column(nullable = false)
    private Boolean active;
    @Column(nullable = false)
    private Boolean movable;

    public TransitStop() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMbtaStopId() { return mbtaStopId; }
    public void setMbtaStopId(String mbtaStopId) { this.mbtaStopId = mbtaStopId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Boolean getMovable() { return movable; }
    public void setMovable(Boolean movable) { this.movable = movable; }
}
