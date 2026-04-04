package com.izikwen.mbtaoptimizer.dto.response;

import java.util.List;

public class NetworkResponse {
    private List<LineDto> lines;

    public NetworkResponse() {}
    public NetworkResponse(List<LineDto> lines) { this.lines = lines; }

    public List<LineDto> getLines() { return lines; }
    public void setLines(List<LineDto> lines) { this.lines = lines; }

    public static class LineDto {
        private Long lineId;
        private String mbtaRouteId;
        private String lineName;
        private String color;
        private String mode;
        private List<StopDto> stops;
        private List<EdgeDto> edges;

        public LineDto() {}

        public Long getLineId() { return lineId; }
        public void setLineId(Long lineId) { this.lineId = lineId; }
        public String getMbtaRouteId() { return mbtaRouteId; }
        public void setMbtaRouteId(String mbtaRouteId) { this.mbtaRouteId = mbtaRouteId; }
        public String getLineName() { return lineName; }
        public void setLineName(String lineName) { this.lineName = lineName; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }
        public List<StopDto> getStops() { return stops; }
        public void setStops(List<StopDto> stops) { this.stops = stops; }
        public List<EdgeDto> getEdges() { return edges; }
        public void setEdges(List<EdgeDto> edges) { this.edges = edges; }
    }

    public static class StopDto {
        private Long stopId;
        private String mbtaStopId;
        private String name;
        private Double lat;
        private Double lng;

        public StopDto() {}

        public StopDto(Long stopId, String mbtaStopId, String name, Double lat, Double lng) {
            this.stopId = stopId;
            this.mbtaStopId = mbtaStopId;
            this.name = name;
            this.lat = lat;
            this.lng = lng;
        }

        public Long getStopId() { return stopId; }
        public void setStopId(Long stopId) { this.stopId = stopId; }
        public String getMbtaStopId() { return mbtaStopId; }
        public void setMbtaStopId(String mbtaStopId) { this.mbtaStopId = mbtaStopId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Double getLat() { return lat; }
        public void setLat(Double lat) { this.lat = lat; }
        public Double getLng() { return lng; }
        public void setLng(Double lng) { this.lng = lng; }
    }

    public static class EdgeDto {
        private Long fromStopId;
        private Long toStopId;
        private Double distanceMeters;

        public EdgeDto() {}

        public EdgeDto(Long fromStopId, Long toStopId, Double distanceMeters) {
            this.fromStopId = fromStopId;
            this.toStopId = toStopId;
            this.distanceMeters = distanceMeters;
        }

        public Long getFromStopId() { return fromStopId; }
        public void setFromStopId(Long fromStopId) { this.fromStopId = fromStopId; }
        public Long getToStopId() { return toStopId; }
        public void setToStopId(Long toStopId) { this.toStopId = toStopId; }
        public Double getDistanceMeters() { return distanceMeters; }
        public void setDistanceMeters(Double distanceMeters) { this.distanceMeters = distanceMeters; }
    }
}
