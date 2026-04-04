package com.izikwen.mbtaoptimizer.dto.request;

import jakarta.validation.constraints.NotNull;

public class MoveStopRequest {
    @NotNull
    private Long stopId;
    @NotNull
    private Double newLat;
    @NotNull
    private Double newLng;

    public Long getStopId() { return stopId; }
    public void setStopId(Long stopId) { this.stopId = stopId; }
    public Double getNewLat() { return newLat; }
    public void setNewLat(Double newLat) { this.newLat = newLat; }
    public Double getNewLng() { return newLng; }
    public void setNewLng(Double newLng) { this.newLng = newLng; }
}
