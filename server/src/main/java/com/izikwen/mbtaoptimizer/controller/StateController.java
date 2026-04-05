package com.izikwen.mbtaoptimizer.controller;

import com.izikwen.mbtaoptimizer.service.TransitState;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/state")
public class StateController {

    private final TransitState state;

    public StateController(TransitState state) {
        this.state = state;
    }

    /** GET /api/state — summary of all cached transit data + score stats. */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(state.getSummary());
    }

    /** POST /api/state/refresh — force reload all data from MBTA API. */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh() {
        state.refresh();
        return ResponseEntity.ok(state.getSummary());
    }

    /** GET /api/state/scores — list of score computation snapshots. */
    @GetMapping("/scores")
    public ResponseEntity<List<TransitState.ScoreSnapshotSummary>> scoreHistory() {
        return ResponseEntity.ok(state.getScoreHistory());
    }

    /** GET /api/state/scores/{id} — full detail for one score snapshot. */
    @GetMapping("/scores/{id}")
    public ResponseEntity<TransitState.ScoreSnapshot> scoreSnapshot(@PathVariable String id) {
        return state.getScoreSnapshot(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** GET /api/state/stops — all cached stops. */
    @GetMapping("/stops")
    public ResponseEntity<List<TransitState.Stop>> stops() {
        return ResponseEntity.ok(state.getStops());
    }

    /** GET /api/state/lines — all cached lines. */
    @GetMapping("/lines")
    public ResponseEntity<Map<String, TransitState.Line>> lines() {
        return ResponseEntity.ok(state.getLines());
    }
}
