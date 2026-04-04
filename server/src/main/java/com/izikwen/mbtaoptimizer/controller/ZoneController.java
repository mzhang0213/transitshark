package com.izikwen.mbtaoptimizer.controller;

import com.izikwen.mbtaoptimizer.dto.response.ZoneInfoResponse;
import com.izikwen.mbtaoptimizer.dto.response.ZoneScoreResponse;
import com.izikwen.mbtaoptimizer.entity.Zone;
import com.izikwen.mbtaoptimizer.repository.ZoneRepository;
import com.izikwen.mbtaoptimizer.service.ZoneScoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zones")
public class ZoneController {

    private final ZoneRepository zoneRepository;
    private final ZoneScoreService zoneScoreService;

    public ZoneController(ZoneRepository zoneRepository, ZoneScoreService zoneScoreService) {
        this.zoneRepository = zoneRepository;
        this.zoneScoreService = zoneScoreService;
    }

    /**
     * GET /api/zones
     * Returns all zones with their location coordinates.
     */
    @GetMapping
    public ResponseEntity<List<ZoneInfoResponse>> getZones() {
        List<Zone> zones = zoneRepository.findAllByOrderByZoneIdAsc();
        List<ZoneInfoResponse> result = zones.stream()
                .map(z -> new ZoneInfoResponse(
                        z.getZoneId(), z.getName(),
                        z.getCenterLat(), z.getCenterLng(),
                        z.getMinLat(), z.getMinLng(),
                        z.getMaxLat(), z.getMaxLng()))
                .toList();
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/zones/scores
     * Calculates and returns a service score for every zone.
     */
    @GetMapping("/scores")
    public ResponseEntity<List<ZoneScoreResponse>> calculateAllZoneScores() {
        return ResponseEntity.ok(zoneScoreService.calculateAllZoneScores());
    }
}
