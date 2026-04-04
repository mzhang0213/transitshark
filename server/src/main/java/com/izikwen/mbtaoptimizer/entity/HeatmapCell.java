package com.izikwen.mbtaoptimizer.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "heatmap_cells")
public class HeatmapCell {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String areaCode;
    @Column(nullable = false)
    private Double lat;
    @Column(nullable = false)
    private Double lng;
    @Column(nullable = false)
    private Double intensity;
    @Column(nullable = false)
    private Integer hourOfDay;
    @Column(nullable = false, length = 50)
    private String metricType;

    public HeatmapCell() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAreaCode() { return areaCode; }
    public void setAreaCode(String areaCode) { this.areaCode = areaCode; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public Double getIntensity() { return intensity; }
    public void setIntensity(Double intensity) { this.intensity = intensity; }
    public Integer getHourOfDay() { return hourOfDay; }
    public void setHourOfDay(Integer hourOfDay) { this.hourOfDay = hourOfDay; }
    public String getMetricType() { return metricType; }
    public void setMetricType(String metricType) { this.metricType = metricType; }
}
