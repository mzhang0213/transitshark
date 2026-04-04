package com.izikwen.mbtaoptimizer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ModifyStopRequest {

    @NotNull(message = "stopId is required")
    private Long stopId;

    @NotBlank(message = "modificationType is required (MOVE or DELETE)")
    private String modificationType;

    private ModificationPayload modification;

    public Long getStopId() { return stopId; }
    public void setStopId(Long stopId) { this.stopId = stopId; }
    public String getModificationType() { return modificationType; }
    public void setModificationType(String modificationType) { this.modificationType = modificationType; }
    public ModificationPayload getModification() { return modification; }
    public void setModification(ModificationPayload modification) { this.modification = modification; }

    public static class ModificationPayload {
        private Double newLat;
        private Double newLng;

        public Double getNewLat() { return newLat; }
        public void setNewLat(Double newLat) { this.newLat = newLat; }
        public Double getNewLng() { return newLng; }
        public void setNewLng(Double newLng) { this.newLng = newLng; }
    }
}
