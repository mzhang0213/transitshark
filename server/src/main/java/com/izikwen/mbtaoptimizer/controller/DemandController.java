package com.izikwen.mbtaoptimizer.controller;

import com.izikwen.mbtaoptimizer.dto.response.StopDemandResponse;
import com.izikwen.mbtaoptimizer.service.DemandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ridership-demand")
public class DemandController {

    private final DemandService demandService;

    public DemandController(DemandService demandService) {
        this.demandService = demandService;
    }

    /**
     * GET /api/ridership-demand?timeOfDay=13
     * Returns per-stop ridership demand for the given hour.
     */
    @GetMapping
    public ResponseEntity<List<StopDemandResponse>> getRidershipDemand(
            @RequestParam(defaultValue = "13") Integer timeOfDay) {
        return ResponseEntity.ok(demandService.getRidershipDemand(timeOfDay));
    }
}
