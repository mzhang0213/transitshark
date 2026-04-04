package com.izikwen.mbtaoptimizer.service;

import com.izikwen.mbtaoptimizer.dto.response.StopDemandResponse;
import com.izikwen.mbtaoptimizer.entity.DemandSnapshot;
import com.izikwen.mbtaoptimizer.entity.TransitStop;
import com.izikwen.mbtaoptimizer.repository.DemandSnapshotRepository;
import com.izikwen.mbtaoptimizer.repository.TransitStopRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DemandService {

    private final TransitStopRepository stopRepository;
    private final DemandSnapshotRepository demandRepository;

    public DemandService(TransitStopRepository stopRepository, DemandSnapshotRepository demandRepository) {
        this.stopRepository = stopRepository;
        this.demandRepository = demandRepository;
    }

    /**
     * Returns per-stop ridership demand for a given time of day.
     * Joins stops with demand snapshots by matching stop zoneId to demand areaCode.
     */
    public List<StopDemandResponse> getRidershipDemand(Integer timeOfDay) {
        List<TransitStop> stops = stopRepository.findByActiveTrue();
        List<DemandSnapshot> snapshots = demandRepository.findByHourOfDay(timeOfDay);

        Map<String, Double> demandByArea = snapshots.stream()
                .collect(Collectors.groupingBy(
                        DemandSnapshot::getAreaCode,
                        Collectors.averagingDouble(DemandSnapshot::getDemandScore)
                ));

        List<StopDemandResponse> result = new ArrayList<>();
        for (TransitStop stop : stops) {
            Double demand = stop.getZoneId() != null
                    ? demandByArea.getOrDefault(stop.getZoneId(), 0.0)
                    : 0.0;
            result.add(new StopDemandResponse(
                    stop.getId(), stop.getMbtaStopId(), stop.getName(),
                    demand, stop.getLat(), stop.getLng()
            ));
        }
        return result;
    }
}
