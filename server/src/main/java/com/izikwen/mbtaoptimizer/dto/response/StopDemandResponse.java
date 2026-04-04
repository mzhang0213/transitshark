package com.izikwen.mbtaoptimizer.dto.response;

public class StopDemandResponse {
    private Long stopId;
    private String mbtaStopId;
    private String name;
    private Double demand;
    private Double lat;
    private Double lng;

    public StopDemandResponse() {}

    public StopDemandResponse(Long stopId, String mbtaStopId, String name, Double demand, Double lat, Double lng) {
        this.stopId = stopId;
        this.mbtaStopId = mbtaStopId;
        this.name = name;
        this.demand = demand;
        this.lat = lat;
        this.lng = lng;
    }

    public Long getStopId() { return stopId; }
    public void setStopId(Long stopId) { this.stopId = stopId; }
    public String getMbtaStopId() { return mbtaStopId; }
    public void setMbtaStopId(String mbtaStopId) { this.mbtaStopId = mbtaStopId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getDemand() { return demand; }
    public void setDemand(Double demand) { this.demand = demand; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
}
