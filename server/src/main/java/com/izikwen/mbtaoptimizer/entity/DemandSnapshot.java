package com.izikwen.mbtaoptimizer.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "demand_snapshots")
public class DemandSnapshot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String areaCode;
    @Column(nullable = false)
    private Integer hourOfDay;
    @Column(nullable = false)
    private Integer dayOfWeek;
    @Column(nullable = false)
    private Double demandScore;
    @Column(nullable = false)
    private Integer estimatedRiders;
    @Column(nullable = false)
    private Double carReplacementPotential;

    public DemandSnapshot() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAreaCode() { return areaCode; }
    public void setAreaCode(String areaCode) { this.areaCode = areaCode; }
    public Integer getHourOfDay() { return hourOfDay; }
    public void setHourOfDay(Integer hourOfDay) { this.hourOfDay = hourOfDay; }
    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public Double getDemandScore() { return demandScore; }
    public void setDemandScore(Double demandScore) { this.demandScore = demandScore; }
    public Integer getEstimatedRiders() { return estimatedRiders; }
    public void setEstimatedRiders(Integer estimatedRiders) { this.estimatedRiders = estimatedRiders; }
    public Double getCarReplacementPotential() { return carReplacementPotential; }
    public void setCarReplacementPotential(Double carReplacementPotential) { this.carReplacementPotential = carReplacementPotential; }
}
