package com.izikwen.mbtaoptimizer.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "optimization_results")
public class OptimizationResult {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "scenario_id", nullable = false, unique = true)
    private OptimizationScenario scenario;
    @Column(nullable = false)
    private Double score;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal estimatedCost;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal estimatedAnnualSavings;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal estimatedAnnualProfit;
    @Column(nullable = false)
    private Double avgTravelTimeReductionPct;
    @Column(nullable = false)
    private Double avgWaitTimeReductionPct;
    @Column(nullable = false)
    private Double co2ReductionEstimate;
    @Column(length = 5000)
    private String summary;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public OptimizationResult() {}

    @PrePersist
    public void prePersist() { this.createdAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public OptimizationScenario getScenario() { return scenario; }
    public void setScenario(OptimizationScenario scenario) { this.scenario = scenario; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }
    public BigDecimal getEstimatedAnnualSavings() { return estimatedAnnualSavings; }
    public void setEstimatedAnnualSavings(BigDecimal estimatedAnnualSavings) { this.estimatedAnnualSavings = estimatedAnnualSavings; }
    public BigDecimal getEstimatedAnnualProfit() { return estimatedAnnualProfit; }
    public void setEstimatedAnnualProfit(BigDecimal estimatedAnnualProfit) { this.estimatedAnnualProfit = estimatedAnnualProfit; }
    public Double getAvgTravelTimeReductionPct() { return avgTravelTimeReductionPct; }
    public void setAvgTravelTimeReductionPct(Double avgTravelTimeReductionPct) { this.avgTravelTimeReductionPct = avgTravelTimeReductionPct; }
    public Double getAvgWaitTimeReductionPct() { return avgWaitTimeReductionPct; }
    public void setAvgWaitTimeReductionPct(Double avgWaitTimeReductionPct) { this.avgWaitTimeReductionPct = avgWaitTimeReductionPct; }
    public Double getCo2ReductionEstimate() { return co2ReductionEstimate; }
    public void setCo2ReductionEstimate(Double co2ReductionEstimate) { this.co2ReductionEstimate = co2ReductionEstimate; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
