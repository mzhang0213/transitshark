package com.izikwen.mbtaoptimizer.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CreateScenarioRequest {
    @NotBlank
    private String name;
    private String areaName;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }
}
