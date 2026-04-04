package com.izikwen.mbtaoptimizer.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "scenario_changes")
public class ScenarioChange {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "scenario_id", nullable = false)
    private OptimizationScenario scenario;
    @Column(nullable = false, length = 100)
    private String changeType;
    @Column(nullable = false, columnDefinition = "text")
    private String payloadJson;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public ScenarioChange() {}

    @PrePersist
    public void prePersist() { this.createdAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public OptimizationScenario getScenario() { return scenario; }
    public void setScenario(OptimizationScenario scenario) { this.scenario = scenario; }
    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
