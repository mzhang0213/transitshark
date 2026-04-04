package com.izikwen.mbtaoptimizer.service;

import com.izikwen.mbtaoptimizer.dto.response.StopDemandResponse;
import com.izikwen.mbtaoptimizer.entity.DemandSnapshot;
import com.izikwen.mbtaoptimizer.entity.TransitStop;
import com.izikwen.mbtaoptimizer.entity.RouteSegment;
import com.izikwen.mbtaoptimizer.repository.DemandSnapshotRepository;
import com.izikwen.mbtaoptimizer.repository.TransitStopRepository;
import com.izikwen.mbtaoptimizer.repository.RouteSegmentRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OptService {

    private final TransitStopRepository stopRepository;
    private final DemandSnapshotRepository demandRepository;
    private final RouteSegmentRepository segmentRepository;

    public OptService(TransitStopRepository stopRepository,
                      DemandSnapshotRepository demandRepository,
                      RouteSegmentRepository segmentRepository) {
        this.stopRepository = stopRepository;
        this.demandRepository = demandRepository;
        this.segmentRepository = segmentRepository;
    }

    /**
     * Returns all stops with a demand score that factors in connectivity.
     * Stops with high demand but low connectivity are ranked higher (more need for optimization).
     */
    public List<StopDemandResponse> getOptimizationData(Integer timeOfDay) {
        List<TransitStop> allStops = stopRepository.findByActiveTrueOrderByNameAsc();
        List<DemandSnapshot> snapshots = demandRepository.findByHourOfDay(timeOfDay);
        List<RouteSegment> allSegments = segmentRepository.findAll();

        Map<String, Double> demandByArea = snapshots.stream()
                .collect(Collectors.groupingBy(
                        DemandSnapshot::getAreaCode,
                        Collectors.averagingDouble(DemandSnapshot::getDemandScore)
                ));

        // Count how many segments touch each stop (measure of connectivity)
        Map<Long, Long> connectivityByStop = new HashMap<>();
        for (RouteSegment seg : allSegments) {
            connectivityByStop.merge(seg.getFromStop().getId(), 1L, Long::sum);
            connectivityByStop.merge(seg.getToStop().getId(), 1L, Long::sum);
        }

        List<StopDemandResponse> result = new ArrayList<>();
        for (TransitStop stop : allStops) {
            double rawDemand = stop.getZoneId() != null
                    ? demandByArea.getOrDefault(stop.getZoneId(), 0.0)
                    : 0.0;

            long connectivity = connectivityByStop.getOrDefault(stop.getId(), 0L);

            // Optimization demand: high raw demand + low connectivity = high optimization need
            double optDemand = rawDemand * (1.0 + 1.0 / (1.0 + connectivity));
            optDemand = Math.round(optDemand * 100.0) / 100.0;

            result.add(new StopDemandResponse(
                    stop.getId(), stop.getMbtaStopId(), stop.getName(),
                    optDemand, stop.getLat(), stop.getLng()
            ));
        }

        // Sort descending by demand so highest-need stops come first
        result.sort(Comparator.comparingDouble(StopDemandResponse::getDemand).reversed());
        return result;
    }
}
