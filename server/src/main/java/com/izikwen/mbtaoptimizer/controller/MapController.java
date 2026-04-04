package com.izikwen.mbtaoptimizer.controller;

import com.izikwen.mbtaoptimizer.dto.response.HeatmapGridCellResponse;
import com.izikwen.mbtaoptimizer.service.MapLayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/map")
public class MapController {

    private final MapLayerService mapLayerService;

    public MapController(MapLayerService mapLayerService) {
        this.mapLayerService = mapLayerService;
    }

    @GetMapping("/heatmap-grid")
    public ResponseEntity<List<HeatmapGridCellResponse>> getGridHeatmap(
            @RequestParam double minLat,
            @RequestParam double minLng,
            @RequestParam double maxLat,
            @RequestParam double maxLng,
            @RequestParam(defaultValue = "13") Integer hour,
            @RequestParam(defaultValue = "DEMAND") String metricType) {
        
        return ResponseEntity.ok(mapLayerService.getDynamicGridHeatmap(
                minLat, minLng, maxLat, maxLng, hour, metricType));
    }
}