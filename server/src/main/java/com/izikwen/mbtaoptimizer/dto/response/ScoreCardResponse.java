package com.izikwen.mbtaoptimizer.dto.response;

public class ScoreCardResponse {
    private Double score;
    private Double avgTravelTimeReductionPct;
    private Double avgWaitTimeReductionPct;
    private Double co2ReductionEstimate;
    private String estimatedCost;
    private String estimatedAnnualSavings;
    private String estimatedAnnualProfit;
    private String mascotMood;

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public Double getAvgTravelTimeReductionPct() { return avgTravelTimeReductionPct; }
    public void setAvgTravelTimeReductionPct(Double avgTravelTimeReductionPct) { this.avgTravelTimeReductionPct = avgTravelTimeReductionPct; }
    public Double getAvgWaitTimeReductionPct() { return avgWaitTimeReductionPct; }
    public void setAvgWaitTimeReductionPct(Double avgWaitTimeReductionPct) { this.avgWaitTimeReductionPct = avgWaitTimeReductionPct; }
    public Double getCo2ReductionEstimate() { return co2ReductionEstimate; }
    public void setCo2ReductionEstimate(Double co2ReductionEstimate) { this.co2ReductionEstimate = co2ReductionEstimate; }
    public String getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(String estimatedCost) { this.estimatedCost = estimatedCost; }
    public String getEstimatedAnnualSavings() { return estimatedAnnualSavings; }
    public void setEstimatedAnnualSavings(String estimatedAnnualSavings) { this.estimatedAnnualSavings = estimatedAnnualSavings; }
    public String getEstimatedAnnualProfit() { return estimatedAnnualProfit; }
    public void setEstimatedAnnualProfit(String estimatedAnnualProfit) { this.estimatedAnnualProfit = estimatedAnnualProfit; }
    public String getMascotMood() { return mascotMood; }
    public void setMascotMood(String mascotMood) { this.mascotMood = mascotMood; }
}
