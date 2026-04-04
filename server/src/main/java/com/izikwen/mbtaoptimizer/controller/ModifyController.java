package com.izikwen.mbtaoptimizer.controller;

import com.izikwen.mbtaoptimizer.dto.request.ModifyLineRequest;
import com.izikwen.mbtaoptimizer.dto.request.ModifyStopRequest;
import com.izikwen.mbtaoptimizer.dto.response.ModifyLineResponse;
import com.izikwen.mbtaoptimizer.dto.response.ZoneScoreResponse;
import com.izikwen.mbtaoptimizer.service.ModifyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ModifyController {

    private final ModifyService modifyService;

    public ModifyController(ModifyService modifyService) {
        this.modifyService = modifyService;
    }

    /**
     * POST /api/modify-stop
     * Modify a stop (MOVE or DELETE) and return recalculated zone scores.
     */
    @PostMapping("/modify-stop")
    public ResponseEntity<List<ZoneScoreResponse>> modifyStop(@Valid @RequestBody ModifyStopRequest request) {
        return ResponseEntity.ok(modifyService.modifyStop(request));
    }

    /**
     * POST /api/modify-line
     * Modify a line (INCR_SERVICE, DECR_SERVICE, ADD_STOP, REMOVE_STOP)
     * and return recalculated zone scores + time window.
     */
    @PostMapping("/modify-line")
    public ResponseEntity<ModifyLineResponse> modifyLine(@Valid @RequestBody ModifyLineRequest request) {
        return ResponseEntity.ok(modifyService.modifyLine(request));
    }
}
