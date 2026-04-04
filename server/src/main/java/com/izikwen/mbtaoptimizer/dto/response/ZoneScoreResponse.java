package com.izikwen.mbtaoptimizer.dto.response;

public class ZoneScoreResponse {
    private String zoneId;
    private String zoneName;
    private Double score;

    public ZoneScoreResponse() {}

    public ZoneScoreResponse(String zoneId, String zoneName, Double score) {
        this.zoneId = zoneId;
        this.zoneName = zoneName;
        this.score = score;
    }

    public String getZoneId() { return zoneId; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }
    public String getZoneName() { return zoneName; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
}
