package com.izikwen.mbtaoptimizer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ChangeFrequencyRequest {
    @NotBlank
    private String routeId;
    @NotNull
    private Integer hourOfDay;
    @NotNull
    private Integer newFrequencyMinutes;

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }
    public Integer getHourOfDay() { return hourOfDay; }
    public void setHourOfDay(Integer hourOfDay) { this.hourOfDay = hourOfDay; }
    public Integer getNewFrequencyMinutes() { return newFrequencyMinutes; }
    public void setNewFrequencyMinutes(Integer newFrequencyMinutes) { this.newFrequencyMinutes = newFrequencyMinutes; }
}
