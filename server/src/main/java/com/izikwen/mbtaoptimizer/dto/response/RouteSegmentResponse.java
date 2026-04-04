package com.izikwen.mbtaoptimizer.dto.response;

public class RouteSegmentResponse {
    private Long id;
    private String routeName;
    private String routeColor;
    private Long fromStopId;
    private Long toStopId;
    private Double distanceMeters;
    private Integer averageTravelTimeSeconds;
    private Integer averageWaitTimeSeconds;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    public String getRouteColor() { return routeColor; }
    public void setRouteColor(String routeColor) { this.routeColor = routeColor; }
    public Long getFromStopId() { return fromStopId; }
    public void setFromStopId(Long fromStopId) { this.fromStopId = fromStopId; }
    public Long getToStopId() { return toStopId; }
    public void setToStopId(Long toStopId) { this.toStopId = toStopId; }
    public Double getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(Double distanceMeters) { this.distanceMeters = distanceMeters; }
    public Integer getAverageTravelTimeSeconds() { return averageTravelTimeSeconds; }
    public void setAverageTravelTimeSeconds(Integer averageTravelTimeSeconds) { this.averageTravelTimeSeconds = averageTravelTimeSeconds; }
    public Integer getAverageWaitTimeSeconds() { return averageWaitTimeSeconds; }
    public void setAverageWaitTimeSeconds(Integer averageWaitTimeSeconds) { this.averageWaitTimeSeconds = averageWaitTimeSeconds; }
}
