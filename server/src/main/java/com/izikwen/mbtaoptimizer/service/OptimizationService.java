package com.izikwen.mbtaoptimizer.service;

import com.izikwen.mbtaoptimizer.client.PythonOptimizerClient;
import com.izikwen.mbtaoptimizer.dto.response.OptimizationResultResponse;
import com.izikwen.mbtaoptimizer.dto.response.ScoreCardResponse;
import com.izikwen.mbtaoptimizer.dto.response.SuggestionDetailsResponse;
import com.izikwen.mbtaoptimizer.entity.OptimizationResult;
import com.izikwen.mbtaoptimizer.entity.OptimizationScenario;
import com.izikwen.mbtaoptimizer.entity.ScenarioChange;
import com.izikwen.mbtaoptimizer.exception.ResourceNotFoundException;
import com.izikwen.mbtaoptimizer.repository.OptimizationResultRepository;
import com.izikwen.mbtaoptimizer.repository.OptimizationScenarioRepository;
import com.izikwen.mbtaoptimizer.repository.ScenarioChangeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class OptimizationService {
    private final OptimizationScenarioRepository scenarioRepository;
    private final ScenarioChangeRepository changeRepository;
    private final OptimizationResultRepository resultRepository;
    private final SharkMascotService sharkMascotService;
    private final PythonOptimizerClient pythonOptimizerClient;

    public OptimizationService(OptimizationScenarioRepository scenarioRepository,
                               ScenarioChangeRepository changeRepository,
                               OptimizationResultRepository resultRepository,
                               SharkMascotService sharkMascotService,
                               PythonOptimizerClient pythonOptimizerClient) {
        this.scenarioRepository = scenarioRepository;
        this.changeRepository = changeRepository;
        this.resultRepository = resultRepository;
        this.sharkMascotService = sharkMascotService;
        this.pythonOptimizerClient = pythonOptimizerClient;
    }

    public OptimizationResultResponse runOptimization(Long scenarioId) {
        OptimizationScenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Scenario not found: " + scenarioId));

        List<ScenarioChange> changes = changeRepository.findByScenario_IdOrderByCreatedAtAsc(scenarioId);
        int changeCount = changes.size();

        double score = Math.min(100.0, 58.0 + (changeCount * 7.0));
        double timeReduction = Math.min(38.0, changeCount * 3.5);
        double waitReduction = Math.min(30.0, changeCount * 2.8);
        double co2Reduction = Math.min(22.0, changeCount * 1.6);

        BigDecimal estimatedCost = BigDecimal.valueOf(220000L + (changeCount * 60000L));
        BigDecimal annualSavings = BigDecimal.valueOf(175000L + (changeCount * 95000L));
        BigDecimal annualProfit = annualSavings.subtract(estimatedCost.multiply(BigDecimal.valueOf(0.30)));

        OptimizationResult result = resultRepository.findByScenario_Id(scenarioId).orElseGet(() -> {
            OptimizationResult r = new OptimizationResult();
            r.setScenario(scenario);
            return r;
        });

        result.setScore(score);
        result.setEstimatedCost(estimatedCost);
        result.setEstimatedAnnualSavings(annualSavings);
        result.setEstimatedAnnualProfit(annualProfit);
        result.setAvgTravelTimeReductionPct(timeReduction);
        result.setAvgWaitTimeReductionPct(waitReduction);
        result.setCo2ReductionEstimate(co2Reduction);
        result.setSummary("Optimization complete. Proposed changes improve travel efficiency, reduce wait times, and increase projected network performance.");
        resultRepository.save(result);

        scenario.setStatus("COMPLETED");
        scenarioRepository.save(scenario);

        ScoreCardResponse scoreCard = new ScoreCardResponse();
        scoreCard.setScore(score);
        scoreCard.setAvgTravelTimeReductionPct(timeReduction);
        scoreCard.setAvgWaitTimeReductionPct(waitReduction);
        scoreCard.setCo2ReductionEstimate(co2Reduction);
        scoreCard.setEstimatedCost(estimatedCost.toPlainString());
        scoreCard.setEstimatedAnnualSavings(annualSavings.toPlainString());
        scoreCard.setEstimatedAnnualProfit(annualProfit.toPlainString());
        scoreCard.setMascotMood(sharkMascotService.moodForScore(score));

        OptimizationResultResponse response = new OptimizationResultResponse();
        response.setScenarioId(scenarioId);
        response.setStatus(scenario.getStatus());
        response.setSummary(result.getSummary());
        response.setScoreCard(scoreCard);
        return response;
    }

    public SuggestionDetailsResponse getSuggestionDetails(Long scenarioId) {
        OptimizationScenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Scenario not found: " + scenarioId));
        OptimizationResult result = resultRepository.findByScenario_Id(scenarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Optimization result not found. Run optimization first for scenario: " + scenarioId));
        int changeCount = changeRepository.findByScenario_IdOrderByCreatedAtAsc(scenarioId).size();

        Map<String, Object> pythonResponse = pythonOptimizerClient.optimizeScenario(scenarioId, changeCount, scenario.getAreaName());

        @SuppressWarnings("unchecked")
        List<String> recommendedChanges = (List<String>) pythonResponse.getOrDefault("recommendedChanges", List.of());
        @SuppressWarnings("unchecked")
        List<String> supportingData = (List<String>) pythonResponse.getOrDefault("supportingDataPoints", List.of());
        @SuppressWarnings("unchecked")
        List<String> tradeoffs = (List<String>) pythonResponse.getOrDefault("tradeoffs", List.of());

        SuggestionDetailsResponse response = new SuggestionDetailsResponse();
        response.setScenarioId(scenarioId);
        response.setHeadline(String.valueOf(pythonResponse.getOrDefault("headline", "Optimization details")));
        response.setExplanation(String.valueOf(pythonResponse.getOrDefault("explanation", result.getSummary())));
        response.setRecommendedChanges(recommendedChanges);
        response.setSupportingDataPoints(supportingData);
        response.setTradeoffs(tradeoffs);
        return response;
    }
}
