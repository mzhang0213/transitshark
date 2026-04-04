package com.izikwen.mbtaoptimizer.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "transit_routes")
public class TransitRoute {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, length = 100)
    private String mbtaRouteId;
    @Column(length = 100)
    private String shortName;
    @Column(length = 200)
    private String longName;
    @Column(nullable = false, length = 50)
    private String mode;
    @Column(length = 20)
    private String color;

    public TransitRoute() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMbtaRouteId() { return mbtaRouteId; }
    public void setMbtaRouteId(String mbtaRouteId) { this.mbtaRouteId = mbtaRouteId; }
    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }
    public String getLongName() { return longName; }
    public void setLongName(String longName) { this.longName = longName; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
