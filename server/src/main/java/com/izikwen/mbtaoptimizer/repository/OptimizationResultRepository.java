package com.izikwen.mbtaoptimizer.repository;

import com.izikwen.mbtaoptimizer.entity.OptimizationResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OptimizationResultRepository extends JpaRepository<OptimizationResult, Long> {
    Optional<OptimizationResult> findByScenario_Id(Long scenarioId);
}
