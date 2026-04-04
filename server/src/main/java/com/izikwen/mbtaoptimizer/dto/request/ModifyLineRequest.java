package com.izikwen.mbtaoptimizer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ModifyLineRequest {

    @NotNull(message = "line (route id) is required")
    private Long line;

    @NotBlank(message = "modificationType is required (INCR_SERVICE, DECR_SERVICE, ADD_STOP, REMOVE_STOP)")
    private String modificationType;

    private ModificationPayload modification;

    private Integer time;

    public Long getLine() { return line; }
    public void setLine(Long line) { this.line = line; }
    public String getModificationType() { return modificationType; }
    public void setModificationType(String modificationType) { this.modificationType = modificationType; }
    public ModificationPayload getModification() { return modification; }
    public void setModification(ModificationPayload modification) { this.modification = modification; }
    public Integer getTime() { return time; }
    public void setTime(Integer time) { this.time = time; }

    public static class ModificationPayload {
        private Long stopId;
        private String stopName;
        private Double lat;
        private Double lng;
        private Integer frequencyChangeMinutes;

        public Long getStopId() { return stopId; }
        public void setStopId(Long stopId) { this.stopId = stopId; }
        public String getStopName() { return stopName; }
        public void setStopName(String stopName) { this.stopName = stopName; }
        public Double getLat() { return lat; }
        public void setLat(Double lat) { this.lat = lat; }
        public Double getLng() { return lng; }
        public void setLng(Double lng) { this.lng = lng; }
        public Integer getFrequencyChangeMinutes() { return frequencyChangeMinutes; }
        public void setFrequencyChangeMinutes(Integer frequencyChangeMinutes) { this.frequencyChangeMinutes = frequencyChangeMinutes; }
    }
}
