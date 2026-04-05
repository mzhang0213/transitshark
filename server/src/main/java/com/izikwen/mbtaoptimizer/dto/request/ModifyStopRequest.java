package com.izikwen.mbtaoptimizer.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ModifyStopRequest {

    @NotBlank(message = "mbtaStopId is required")
    private String mbtaStopId;

    @NotBlank(message = "modificationType is required (MOVE or DELETE)")
    private String modificationType;

    private ModificationPayload modification;

    public String getMbtaStopId() { return mbtaStopId; }
    public void setMbtaStopId(String mbtaStopId) { this.mbtaStopId = mbtaStopId; }
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
