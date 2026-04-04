package com.izikwen.mbtaoptimizer.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "zones")
public class Zone {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String zoneId;

    @Column(length = 200)
    private String name;

    @Column(nullable = false)
    private Double centerLat;

    @Column(nullable = false)
    private Double centerLng;

    @Column(nullable = false)
    private Double minLat;

    @Column(nullable = false)
    private Double minLng;

    @Column(nullable = false)
    private Double maxLat;

    @Column(nullable = false)
    private Double maxLng;

    public Zone() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getZoneId() { return zoneId; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getCenterLat() { return centerLat; }
    public void setCenterLat(Double centerLat) { this.centerLat = centerLat; }
    public Double getCenterLng() { return centerLng; }
    public void setCenterLng(Double centerLng) { this.centerLng = centerLng; }
    public Double getMinLat() { return minLat; }
    public void setMinLat(Double minLat) { this.minLat = minLat; }
    public Double getMinLng() { return minLng; }
    public void setMinLng(Double minLng) { this.minLng = minLng; }
    public Double getMaxLat() { return maxLat; }
    public void setMaxLat(Double maxLat) { this.maxLat = maxLat; }
    public Double getMaxLng() { return maxLng; }
    public void setMaxLng(Double maxLng) { this.maxLng = maxLng; }
}
