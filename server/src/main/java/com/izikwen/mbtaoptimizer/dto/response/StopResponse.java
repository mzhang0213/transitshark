package com.izikwen.mbtaoptimizer.dto.response;

public class StopResponse {
    private Long id;
    private String mbtaStopId;
    private String name;
    private String mode;
    private Double lat;
    private Double lng;
    private Boolean active;
    private Boolean movable;

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
