package com.izikwen.mbtaoptimizer.controller;

import com.izikwen.mbtaoptimizer.service.MbtaSyncService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mbta")
public class MbtaController {
    private final MbtaSyncService mbtaSyncService;

    public MbtaController(MbtaSyncService mbtaSyncService) {
        this.mbtaSyncService = mbtaSyncService;
    }

    @GetMapping("/stops/raw")
    public String stopsRaw() { return mbtaSyncService.fetchStopsRaw(); }

    @GetMapping("/routes/raw")
    public String routesRaw() { return mbtaSyncService.fetchRoutesRaw(); }

    @GetMapping("/predictions/raw")
    public String predictionsRaw() { return mbtaSyncService.fetchPredictionsRaw(); }
}
