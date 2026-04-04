package com.izikwen.mbtaoptimizer.controller;

import com.izikwen.mbtaoptimizer.dto.request.AddStopRequest;
import com.izikwen.mbtaoptimizer.dto.request.ChangeFrequencyRequest;
import com.izikwen.mbtaoptimizer.dto.request.CreateScenarioRequest;
import com.izikwen.mbtaoptimizer.dto.request.MoveStopRequest;
import com.izikwen.mbtaoptimizer.dto.response.ScenarioResponse;
import com.izikwen.mbtaoptimizer.service.ScenarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scenarios")
public class ScenarioController {
    private final ScenarioService scenarioService;

    public ScenarioController(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }

    @PostMapping
    public ScenarioResponse createScenario(@Valid @RequestBody CreateScenarioRequest request) {
        return scenarioService.createScenario(request);
    }

    @PostMapping("/{scenarioId}/move-stop")
    public ResponseEntity<Void> moveStop(@PathVariable Long scenarioId, @Valid @RequestBody MoveStopRequest request) {
        scenarioService.moveStop(scenarioId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{scenarioId}/add-stop")
    public ResponseEntity<Void> addStop(@PathVariable Long scenarioId, @Valid @RequestBody AddStopRequest request) {
        scenarioService.addStop(scenarioId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{scenarioId}/change-frequency")
    public ResponseEntity<Void> changeFrequency(@PathVariable Long scenarioId, @Valid @RequestBody ChangeFrequencyRequest request) {
        scenarioService.changeFrequency(scenarioId, request);
        return ResponseEntity.ok().build();
    }
}
