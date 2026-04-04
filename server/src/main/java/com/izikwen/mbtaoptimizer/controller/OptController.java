package com.izikwen.mbtaoptimizer.controller;

import com.izikwen.mbtaoptimizer.dto.response.StopDemandResponse;
import com.izikwen.mbtaoptimizer.service.OptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/optimization")
public class OptController {

    private final OptService optService;

    public OptController(OptService optService) {
        this.optService = optService;
    }

    /**
     * GET /api/optimization?timeOfDay=13
     * Returns all stops ranked by optimization need (demand vs connectivity).
     */
    @GetMapping
    public ResponseEntity<List<StopDemandResponse>> getOptimization(
            @RequestParam(defaultValue = "13") Integer timeOfDay) {
        return ResponseEntity.ok(optService.getOptimizationData(timeOfDay));
    }
}
