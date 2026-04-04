package com.izikwen.mbtaoptimizer.repository;

import com.izikwen.mbtaoptimizer.entity.HeatmapCell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HeatmapCellRepository extends JpaRepository<HeatmapCell, Long> {
    List<HeatmapCell> findByLatBetweenAndLngBetweenAndHourOfDayAndMetricTypeIgnoreCase(
            double minLat, double maxLat,
            double minLng, double maxLng,
            Integer hourOfDay, String metricType);
}
