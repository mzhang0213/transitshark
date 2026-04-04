package com.izikwen.mbtaoptimizer.controller;

import com.izikwen.mbtaoptimizer.dto.response.OptimizationResultResponse;
import com.izikwen.mbtaoptimizer.dto.response.SuggestionDetailsResponse;
import com.izikwen.mbtaoptimizer.service.OptimizationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/optimization")
public class OptimizationController {
    private final OptimizationService optimizationService;

    public OptimizationController(OptimizationService optimizationService) {
        this.optimizationService = optimizationService;
    }

    @PostMapping("/{scenarioId}/run")
    public OptimizationResultResponse run(@PathVariable Long scenarioId) {
        return optimizationService.runOptimization(scenarioId);
    }

    @GetMapping("/{scenarioId}/details")
    public SuggestionDetailsResponse details(@PathVariable Long scenarioId) {
        return optimizationService.getSuggestionDetails(scenarioId);
    }
}
