package com.izikwen.mbtaoptimizer.dto.response;

import java.util.List;

public class HeatmapLayerResponse {
    private String areaCode;
    private Integer hourOfDay;
    private String metricType;
    private List<HeatmapPointResponse> points;

    public String getAreaCode() { return areaCode; }
    public void setAreaCode(String areaCode) { this.areaCode = areaCode; }
    public Integer getHourOfDay() { return hourOfDay; }
    public void setHourOfDay(Integer hourOfDay) { this.hourOfDay = hourOfDay; }
    public String getMetricType() { return metricType; }
    public void setMetricType(String metricType) { this.metricType = metricType; }
    public List<HeatmapPointResponse> getPoints() { return points; }
    public void setPoints(List<HeatmapPointResponse> points) { this.points = points; }
}
