package com.izikwen.mbtaoptimizer.repository;

import com.izikwen.mbtaoptimizer.entity.HeatmapCell;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HeatmapCellRepository extends JpaRepository<HeatmapCell, Long> {
    List<HeatmapCell> findByAreaCodeIgnoreCaseAndHourOfDayAndMetricTypeIgnoreCase(String areaCode, Integer hourOfDay, String metricType);
}
