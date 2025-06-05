package com.topographe.topographe.service.referentiel;

import com.topographe.topographe.dto.referentiel.CityDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CityService {
    CityDto getCityById(Long id);
    void deleteCity(Long id);
    Page<CityDto> getAllCities(Pageable pageable);
}
