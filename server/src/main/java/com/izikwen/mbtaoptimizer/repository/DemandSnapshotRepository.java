package com.izikwen.mbtaoptimizer.repository;

import com.izikwen.mbtaoptimizer.entity.DemandSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DemandSnapshotRepository extends JpaRepository<DemandSnapshot, Long> {
    List<DemandSnapshot> findByAreaCodeIgnoreCase(String areaCode);
    List<DemandSnapshot> findByHourOfDay(Integer hourOfDay);
}
