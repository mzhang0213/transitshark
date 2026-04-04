package com.izikwen.mbtaoptimizer.dto.response;

public class ZoneInfoResponse {
    private String zoneId;
    private String name;
    private Double centerLat;
    private Double centerLng;
    private Double minLat;
    private Double minLng;
    private Double maxLat;
    private Double maxLng;

    public ZoneInfoResponse() {}

    public ZoneInfoResponse(String zoneId, String name,
                            Double centerLat, Double centerLng,
                            Double minLat, Double minLng,
                            Double maxLat, Double maxLng) {
        this.zoneId = zoneId;
        this.name = name;
        this.centerLat = centerLat;
        this.centerLng = centerLng;
        this.minLat = minLat;
        this.minLng = minLng;
        this.maxLat = maxLat;
        this.maxLng = maxLng;
    }

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
