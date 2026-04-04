package com.izikwen.mbtaoptimizer.client;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class PythonOptimizerClient {

    public Map<String, Object> optimizeScenario(Long scenarioId, int changeCount, String areaName) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("headline", "Suggested optimization for " + (areaName == null || areaName.isBlank() ? "selected area" : areaName));
        response.put("explanation", "This is a hackathon-safe placeholder. Replace this class with a real FastAPI or Flask call for optimization output.");
        response.put("recommendedChanges", List.of(
                "Move one low-performing stop closer to demand hotspots.",
                "Reduce midday bus frequency where demand drops.",
                "Reinforce service near consistently crowded transfer points."
        ));
        response.put("supportingDataPoints", List.of(
                "Scenario edits detected: " + changeCount,
                "Estimated demand profile included in score calculation.",
                "Travel-time and wait-time savings weighted into final score."
        ));
        response.put("tradeoffs", List.of(
                "Higher up-front cost if a new stop is added.",
                "Construction or operational changes may need phased rollout.",
                "Savings vary by actual ridership response."
        ));
        return response;
    }
}
