package com.izikwen.mbtaoptimizer.dto.response;

public class HeatmapPointResponse {
    private Double lat;
    private Double lng;
    private Double intensity;

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public Double getIntensity() { return intensity; }
    public void setIntensity(Double intensity) { this.intensity = intensity; }
}
