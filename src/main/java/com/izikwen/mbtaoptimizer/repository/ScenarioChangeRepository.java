package com.izikwen.mbtaoptimizer.repository;

import com.izikwen.mbtaoptimizer.entity.ScenarioChange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScenarioChangeRepository extends JpaRepository<ScenarioChange, Long> {
    List<ScenarioChange> findByScenario_IdOrderByCreatedAtAsc(Long scenarioId);
}
