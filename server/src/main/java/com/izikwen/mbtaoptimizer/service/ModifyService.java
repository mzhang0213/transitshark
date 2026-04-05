package com.izikwen.mbtaoptimizer.service;

import com.izikwen.mbtaoptimizer.dto.request.ModifyLineRequest;
import com.izikwen.mbtaoptimizer.dto.request.ModifyStopRequest;
import com.izikwen.mbtaoptimizer.dto.response.ModifyLineResponse;
import com.izikwen.mbtaoptimizer.dto.response.ZoneScoreResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModifyService {

    private final TransitState state;
    private final ZoneService zoneService;

    public ModifyService(TransitState state, ZoneService zoneService) {
        this.state = state;
        this.zoneService = zoneService;
    }

    /**
     * Modify a stop in the in-memory TransitState and recompute zone scores.
     */
    public List<ZoneScoreResponse> modifyStop(ModifyStopRequest request) {
        String type = request.getModificationType().toUpperCase();
        switch (type) {
            case "MOVE" -> {
                if (request.getModification() == null
                        || request.getModification().getNewLat() == null
                        || request.getModification().getNewLng() == null) {
                    throw new IllegalArgumentException("MOVE requires modification with newLat and newLng");
                }
                boolean found = state.moveStop(
                        request.getMbtaStopId(),
                        request.getModification().getNewLat(),
                        request.getModification().getNewLng());
                if (!found) {
                    throw new IllegalArgumentException("Stop not found in state: " + request.getMbtaStopId());
                }
            }
            default -> throw new IllegalArgumentException(
                    "Unknown modificationType: " + type + ". Use MOVE.");
        }

        return zoneService.getZoneScores();
    }

    /**
     * Modify a line's service level and recompute zone scores.
     * Currently triggers a score recomputation; frequency tracking
     * can be added to TransitState for more granular control.
     */
    public ModifyLineResponse modifyLine(ModifyLineRequest request) {
        // TODO: track per-line service multiplier in TransitState
        // For now, just recompute scores from current state
        List<ZoneScoreResponse> scores = zoneService.getZoneScores();
        return new ModifyLineResponse(scores, request.getTime());
    }
}
