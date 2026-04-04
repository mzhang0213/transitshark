package com.izikwen.mbtaoptimizer.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "route_segments")
public class RouteSegment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "route_id", nullable = false)
    private TransitRoute route;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "from_stop_id", nullable = false)
    private TransitStop fromStop;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "to_stop_id", nullable = false)
    private TransitStop toStop;
    @Column(nullable = false)
    private Double distanceMeters;
    @Column(nullable = false)
    private Integer averageTravelTimeSeconds;
    @Column(nullable = false)
    private Integer averageWaitTimeSeconds;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal operatingCostPerHour;
    @Column(nullable = false)
    private Integer capacity;

    public RouteSegment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TransitRoute getRoute() { return route; }
    public void setRoute(TransitRoute route) { this.route = route; }
    public TransitStop getFromStop() { return fromStop; }
    public void setFromStop(TransitStop fromStop) { this.fromStop = fromStop; }
    public TransitStop getToStop() { return toStop; }
    public void setToStop(TransitStop toStop) { this.toStop = toStop; }
    public Double getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(Double distanceMeters) { this.distanceMeters = distanceMeters; }
    public Integer getAverageTravelTimeSeconds() { return averageTravelTimeSeconds; }
    public void setAverageTravelTimeSeconds(Integer averageTravelTimeSeconds) { this.averageTravelTimeSeconds = averageTravelTimeSeconds; }
    public Integer getAverageWaitTimeSeconds() { return averageWaitTimeSeconds; }
    public void setAverageWaitTimeSeconds(Integer averageWaitTimeSeconds) { this.averageWaitTimeSeconds = averageWaitTimeSeconds; }
    public BigDecimal getOperatingCostPerHour() { return operatingCostPerHour; }
    public void setOperatingCostPerHour(BigDecimal operatingCostPerHour) { this.operatingCostPerHour = operatingCostPerHour; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
}
