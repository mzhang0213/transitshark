package com.izikwen.mbtaoptimizer.service;

import com.izikwen.mbtaoptimizer.dto.response.ZoneDemandResponse;
import com.izikwen.mbtaoptimizer.entity.DemandSnapshot;
import com.izikwen.mbtaoptimizer.entity.Zone;
import com.izikwen.mbtaoptimizer.repository.DemandSnapshotRepository;
import com.izikwen.mbtaoptimizer.repository.ZoneRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HeatmapService {

    private final ZoneRepository zoneRepository;
    private final DemandSnapshotRepository demandRepository;

    public HeatmapService(ZoneRepository zoneRepository, DemandSnapshotRepository demandRepository) {
        this.zoneRepository = zoneRepository;
        this.demandRepository = demandRepository;
    }

    /**
     * Returns demand aggregated by zone for a given time of day.
     * Maps DemandSnapshot areaCode to Zone zoneId.
     */
    public List<ZoneDemandResponse> getHeatmapZones(Integer timeOfDay) {
        List<Zone> zones = zoneRepository.findAllByOrderByZoneIdAsc();
        List<DemandSnapshot> snapshots = demandRepository.findByHourOfDay(timeOfDay);

        Map<String, Double> demandByArea = snapshots.stream()
                .collect(Collectors.groupingBy(
                        DemandSnapshot::getAreaCode,
                        Collectors.averagingDouble(DemandSnapshot::getDemandScore)
                ));

        List<ZoneDemandResponse> result = new ArrayList<>();
        for (Zone zone : zones) {
            Double demand = demandByArea.getOrDefault(zone.getZoneId(), 0.0);
            result.add(new ZoneDemandResponse(
                    zone.getZoneId(), zone.getName(), demand,
                    zone.getCenterLat(), zone.getCenterLng()
            ));
        }
        return result;
    }
}
