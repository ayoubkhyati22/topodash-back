package com.topographe.topographe.repository.referentiel;

import com.topographe.topographe.entity.referentiel.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
}
