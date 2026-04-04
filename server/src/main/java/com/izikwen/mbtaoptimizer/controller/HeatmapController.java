package com.izikwen.mbtaoptimizer.controller;

import com.izikwen.mbtaoptimizer.dto.response.ZoneDemandResponse;
import com.izikwen.mbtaoptimizer.service.HeatmapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/heatmap")
public class HeatmapController {

    private final HeatmapService heatmapService;

    public HeatmapController(HeatmapService heatmapService) {
        this.heatmapService = heatmapService;
    }

    /**
     * GET /api/heatmap?timeOfDay=13
     * Returns demand aggregated by zone (ports stop data to heatmap zones).
     */
    @GetMapping
    public ResponseEntity<List<ZoneDemandResponse>> getHeatmapZones(
            @RequestParam(defaultValue = "13") Integer timeOfDay) {
        return ResponseEntity.ok(heatmapService.getHeatmapZones(timeOfDay));
    }
}
