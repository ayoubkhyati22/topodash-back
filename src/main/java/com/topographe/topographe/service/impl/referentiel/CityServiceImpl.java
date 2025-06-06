package com.topographe.topographe.service.impl.referentiel;

import com.topographe.topographe.dto.referentiel.CityDto;
import com.topographe.topographe.mapper.referentiel.CityMapper;
import com.topographe.topographe.repository.referentiel.CityRepository;
import com.topographe.topographe.service.referentiel.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {
    private final CityRepository cityRepository;

    @Override
    public List<CityDto> getAllCities() {
        return cityRepository.findAll()
                .stream()
                .map(CityMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CityDto getCityById(Long id) {
        return cityRepository.findById(id)
                .map(CityMapper::toDto)
                .orElseThrow(() -> new RuntimeException("City not found"));
    }

    @Override
    public void deleteCity(Long id) {
        cityRepository.deleteById(id);
    }

    @Override
    public Page<CityDto> getAllCities(Pageable pageable) {
        return cityRepository.findAll(pageable).map(CityMapper::toDto);
    }
}
