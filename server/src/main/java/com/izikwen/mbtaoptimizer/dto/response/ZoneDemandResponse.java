package com.izikwen.mbtaoptimizer.dto.response;

public class ZoneDemandResponse {
    private String zoneId;
    private String zoneName;
    private Double demand;
    private Double centerLat;
    private Double centerLng;

    public ZoneDemandResponse() {}

    public ZoneDemandResponse(String zoneId, String zoneName, Double demand, Double centerLat, Double centerLng) {
        this.zoneId = zoneId;
        this.zoneName = zoneName;
        this.demand = demand;
        this.centerLat = centerLat;
        this.centerLng = centerLng;
    }

    public String getZoneId() { return zoneId; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }
    public String getZoneName() { return zoneName; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }
    public Double getDemand() { return demand; }
    public void setDemand(Double demand) { this.demand = demand; }
    public Double getCenterLat() { return centerLat; }
    public void setCenterLat(Double centerLat) { this.centerLat = centerLat; }
    public Double getCenterLng() { return centerLng; }
    public void setCenterLng(Double centerLng) { this.centerLng = centerLng; }
}
