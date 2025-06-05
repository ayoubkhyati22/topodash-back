package com.topographe.topographe.repository.referentiel;

import com.topographe.topographe.entity.referentiel.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
}
