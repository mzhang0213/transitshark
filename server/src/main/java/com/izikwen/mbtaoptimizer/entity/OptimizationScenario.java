package com.izikwen.mbtaoptimizer.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "optimization_scenarios")
public class OptimizationScenario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(length = 150)
    private String areaName;
    @Column(nullable = false, length = 50)
    private String status;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public OptimizationScenario() {}

    @PrePersist
    public void prePersist() { this.createdAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
