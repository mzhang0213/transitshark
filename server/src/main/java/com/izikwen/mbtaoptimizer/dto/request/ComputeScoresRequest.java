package com.izikwen.mbtaoptimizer.dto.request;

import java.util.List;

public class ComputeScoresRequest {

    private List<StopInput> stops;

    public List<StopInput> getStops() { return stops; }
    public void setStops(List<StopInput> stops) { this.stops = stops; }

    public static class StopInput {
        private double lat;
        private double lng;
        private int routeType; // 0=light_rail, 1=heavy_rail, 2=commuter, 3=bus

        public double getLat() { return lat; }
        public void setLat(double lat) { this.lat = lat; }
        public double getLng() { return lng; }
        public void setLng(double lng) { this.lng = lng; }
        public int getRouteType() { return routeType; }
        public void setRouteType(int routeType) { this.routeType = routeType; }
    }
}
