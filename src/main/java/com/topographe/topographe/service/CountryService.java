package com.topographe.topographe.service;

import com.topographe.topographe.dto.CountryDto;
import com.topographe.topographe.entity.Country;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CountryService {
    Country saveCountry(Country country);
    List<Country> getAllCountries();
    CountryDto getCountryById(Long id);
    void deleteCountry(Long id);
    Page<CountryDto> getAllCountries(Pageable pageable);
} 