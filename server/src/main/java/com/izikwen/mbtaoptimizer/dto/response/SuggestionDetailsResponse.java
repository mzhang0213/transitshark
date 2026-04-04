package com.izikwen.mbtaoptimizer.dto.response;

import java.util.List;

public class SuggestionDetailsResponse {
    private Long scenarioId;
    private String headline;
    private String explanation;
    private List<String> recommendedChanges;
    private List<String> supportingDataPoints;
    private List<String> tradeoffs;

    public Long getScenarioId() { return scenarioId; }
    public void setScenarioId(Long scenarioId) { this.scenarioId = scenarioId; }
    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public List<String> getRecommendedChanges() { return recommendedChanges; }
    public void setRecommendedChanges(List<String> recommendedChanges) { this.recommendedChanges = recommendedChanges; }
    public List<String> getSupportingDataPoints() { return supportingDataPoints; }
    public void setSupportingDataPoints(List<String> supportingDataPoints) { this.supportingDataPoints = supportingDataPoints; }
    public List<String> getTradeoffs() { return tradeoffs; }
    public void setTradeoffs(List<String> tradeoffs) { this.tradeoffs = tradeoffs; }
}
