package com.izikwen.mbtaoptimizer.dto.response;

public class OptimizationResultResponse {
    private Long scenarioId;
    private String status;
    private ScoreCardResponse scoreCard;
    private String summary;

    public Long getScenarioId() { return scenarioId; }
    public void setScenarioId(Long scenarioId) { this.scenarioId = scenarioId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public ScoreCardResponse getScoreCard() { return scoreCard; }
    public void setScoreCard(ScoreCardResponse scoreCard) { this.scoreCard = scoreCard; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}
