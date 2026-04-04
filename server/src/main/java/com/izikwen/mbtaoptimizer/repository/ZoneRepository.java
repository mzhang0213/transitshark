package com.izikwen.mbtaoptimizer.repository;

import com.izikwen.mbtaoptimizer.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {
    Optional<Zone> findByZoneId(String zoneId);
    List<Zone> findAllByOrderByZoneIdAsc();
}
