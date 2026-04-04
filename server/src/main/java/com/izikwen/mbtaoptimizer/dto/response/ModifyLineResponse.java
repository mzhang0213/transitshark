package com.izikwen.mbtaoptimizer.dto.response;

import java.util.List;

public class ModifyLineResponse {
    private List<ZoneScoreResponse> zoneScores;
    private Integer time;

    public ModifyLineResponse() {}

    public ModifyLineResponse(List<ZoneScoreResponse> zoneScores, Integer time) {
        this.zoneScores = zoneScores;
        this.time = time;
    }

    public List<ZoneScoreResponse> getZoneScores() { return zoneScores; }
    public void setZoneScores(List<ZoneScoreResponse> zoneScores) { this.zoneScores = zoneScores; }
    public Integer getTime() { return time; }
    public void setTime(Integer time) { this.time = time; }
}
