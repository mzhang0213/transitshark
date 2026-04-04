package com.izikwen.mbtaoptimizer.repository;

import com.izikwen.mbtaoptimizer.entity.RouteSegment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteSegmentRepository extends JpaRepository<RouteSegment, Long> {
    List<RouteSegment> findByRoute_Id(Long routeId);
    List<RouteSegment> findByFromStop_IdOrToStop_Id(Long fromStopId, Long toStopId);
}
