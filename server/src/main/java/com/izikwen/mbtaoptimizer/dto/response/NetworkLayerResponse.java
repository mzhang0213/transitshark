package com.izikwen.mbtaoptimizer.dto.response;

import java.util.List;

public class NetworkLayerResponse {
    private String mode;
    private List<StopResponse> stops;
    private List<RouteSegmentResponse> segments;

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public List<StopResponse> getStops() { return stops; }
    public void setStops(List<StopResponse> stops) { this.stops = stops; }
    public List<RouteSegmentResponse> getSegments() { return segments; }
    public void setSegments(List<RouteSegmentResponse> segments) { this.segments = segments; }
}
