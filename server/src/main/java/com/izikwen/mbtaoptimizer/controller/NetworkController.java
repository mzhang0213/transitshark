package com.izikwen.mbtaoptimizer.controller;

import com.izikwen.mbtaoptimizer.dto.response.NetworkResponse;
import com.izikwen.mbtaoptimizer.service.NetworkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/network")
public class NetworkController {

    private final NetworkService networkService;

    public NetworkController(NetworkService networkService) {
        this.networkService = networkService;
    }

    /**
     * GET /api/network?type=0,1
     * Returns the full transit network from the live MBTA API.
     * Optional type filter: 0=LightRail, 1=HeavyRail, 2=CommuterRail, 3=Bus, 4=Ferry
     * Defaults to subway only (0,1).
     */
    @GetMapping
    public ResponseEntity<NetworkResponse> getNetwork(
            @RequestParam(value = "type", defaultValue = "0,1") String type) {
        return ResponseEntity.ok(networkService.getNetwork(type));
    }
}
