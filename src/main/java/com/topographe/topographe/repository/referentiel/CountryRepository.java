package com.topographe.topographe.repository.referentiel;

import com.topographe.topographe.entity.referentiel.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
} 