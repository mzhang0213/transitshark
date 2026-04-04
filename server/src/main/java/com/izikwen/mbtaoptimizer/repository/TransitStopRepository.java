package com.izikwen.mbtaoptimizer.repository;

import com.izikwen.mbtaoptimizer.entity.TransitStop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransitStopRepository extends JpaRepository<TransitStop, Long> {
    Optional<TransitStop> findByMbtaStopId(String mbtaStopId);
    List<TransitStop> findByZoneId(String zoneId);
    List<TransitStop> findByActiveTrue();
    List<TransitStop> findByActiveTrueOrderByNameAsc();
}
