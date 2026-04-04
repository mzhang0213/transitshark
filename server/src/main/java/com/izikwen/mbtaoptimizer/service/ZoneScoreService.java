package com.izikwen.mbtaoptimizer.service;

import com.izikwen.mbtaoptimizer.dto.response.ZoneScoreResponse;
import com.izikwen.mbtaoptimizer.entity.Zone;
import com.izikwen.mbtaoptimizer.entity.TransitStop;
import com.izikwen.mbtaoptimizer.entity.RouteSegment;
import com.izikwen.mbtaoptimizer.repository.ZoneRepository;
import com.izikwen.mbtaoptimizer.repository.TransitStopRepository;
import com.izikwen.mbtaoptimizer.repository.RouteSegmentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ZoneScoreService {

    private final ZoneRepository zoneRepository;
    private final TransitStopRepository stopRepository;
    private final RouteSegmentRepository segmentRepository;

    public ZoneScoreService(ZoneRepository zoneRepository,
                            TransitStopRepository stopRepository,
                            RouteSegmentRepository segmentRepository) {
        this.zoneRepository = zoneRepository;
        this.stopRepository = stopRepository;
        this.segmentRepository = segmentRepository;
    }

    /**
     * Calculates a coverage/service score for every zone.
     * Score factors:
     *   - Number of active stops in the zone (coverage)
     *   - Number of route segments touching the zone (connectivity)
     *   - Normalized to 0-100 scale
     */
    public List<ZoneScoreResponse> calculateAllZoneScores() {
        List<Zone> zones = zoneRepository.findAllByOrderByZoneIdAsc();
        List<TransitStop> allActiveStops = stopRepository.findByActiveTrue();
        List<RouteSegment> allSegments = segmentRepository.findAll();

        List<ZoneScoreResponse> scores = new ArrayList<>();

        for (Zone zone : zones) {
            List<TransitStop> stopsInZone = allActiveStops.stream()
                    .filter(s -> isInZone(s, zone))
                    .toList();

            int stopCount = stopsInZone.size();

            Set<Long> stopIdsInZone = stopsInZone.stream()
                    .map(TransitStop::getId)
                    .collect(Collectors.toSet());

            long segmentCount = allSegments.stream()
                    .filter(seg -> stopIdsInZone.contains(seg.getFromStop().getId())
                            || stopIdsInZone.contains(seg.getToStop().getId()))
                    .count();

            // Score: weighted combination of stop coverage and connectivity
            // stopCount contribution: up to 50 points (capped at 10 stops)
            // segmentCount contribution: up to 50 points (capped at 20 segments)
            double stopScore = Math.min(50.0, stopCount * 5.0);
            double segScore = Math.min(50.0, segmentCount * 2.5);
            double totalScore = Math.round((stopScore + segScore) * 10.0) / 10.0;

            scores.add(new ZoneScoreResponse(zone.getZoneId(), zone.getName(), totalScore));
        }

        return scores;
    }

    private boolean isInZone(TransitStop stop, Zone zone) {
        return stop.getLat() >= zone.getMinLat() && stop.getLat() <= zone.getMaxLat()
                && stop.getLng() >= zone.getMinLng() && stop.getLng() <= zone.getMaxLng();
    }
}
