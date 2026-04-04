package com.izikwen.mbtaoptimizer.repository;

import com.izikwen.mbtaoptimizer.entity.TransitRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransitRouteRepository extends JpaRepository<TransitRoute, Long> {
    Optional<TransitRoute> findByMbtaRouteId(String mbtaRouteId);
}
