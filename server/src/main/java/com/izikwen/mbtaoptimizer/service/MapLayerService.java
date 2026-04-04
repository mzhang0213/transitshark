package com.izikwen.mbtaoptimizer.service;

import com.izikwen.mbtaoptimizer.entity.HeatmapCell;
import com.izikwen.mbtaoptimizer.repository.HeatmapCellRepository;
import com.izikwen.mbtaoptimizer.dto.response.HeatmapGridCellResponse;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class MapLayerService {

    private final HeatmapCellRepository heatmapCellRepository;

    public MapLayerService(HeatmapCellRepository heatmapCellRepository) {
        this.heatmapCellRepository = heatmapCellRepository;
    }

    public List<HeatmapGridCellResponse> getDynamicGridHeatmap(
            double minLat, double minLng, double maxLat, double maxLng,
            Integer hour, String metricType) {

        List<HeatmapGridCellResponse> gridCells = new ArrayList<>();

        List<HeatmapCell> dbPoints = heatmapCellRepository
                .findByLatBetweenAndLngBetweenAndHourOfDayAndMetricTypeIgnoreCase(
                        minLat, maxLat, minLng, maxLng, hour, metricType);

        double latStep = (maxLat - minLat) / 20;
        double lngStep = (maxLng - minLng) / 20;

        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                double cellMinLat = minLat + (i * latStep);
                double cellMaxLat = cellMinLat + latStep;
                double cellMinLng = minLng + (j * lngStep);
                double cellMaxLng = cellMinLng + lngStep;

                List<HeatmapCell> pointsInCell = dbPoints.stream()
                        .filter(p -> p.getLat() >= cellMinLat && p.getLat() < cellMaxLat &&
                                p.getLng() >= cellMinLng && p.getLng() < cellMaxLng)
                        .toList();

                if (!pointsInCell.isEmpty()) {
                    double avgIntensity = pointsInCell.stream()
                            .mapToDouble(HeatmapCell::getIntensity)
                            .average()
                            .orElse(0.0);

                    gridCells.add(new HeatmapGridCellResponse(
                            cellMinLat, cellMinLng, cellMaxLat, cellMaxLng, avgIntensity
                    ));
                }
            }
        }
        return gridCells;
    }
}