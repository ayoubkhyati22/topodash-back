package com.topographe.topographe.service.referentiel;

import com.topographe.topographe.dto.referentiel.CountryDto;
import com.topographe.topographe.entity.referentiel.Country;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CountryService {
    Country saveCountry(Country country);
    CountryDto getCountryById(Long id);
    void deleteCountry(Long id);
    Page<CountryDto> getAllCountries(Pageable pageable);
} 