package com.topographe.topographe.service.impl.referentiel;

import com.topographe.topographe.dto.referentiel.CountryDto;
import com.topographe.topographe.entity.referentiel.Country;
import com.topographe.topographe.mapper.referentiel.CountryMapper;
import com.topographe.topographe.repository.referentiel.CountryRepository;
import com.topographe.topographe.service.referentiel.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CountryServiceImpl implements CountryService {
    private final CountryRepository countryRepository;

    @Override
    public Country saveCountry(Country country) {
        return countryRepository.save(country);
    }

    @Override
    public List<Country> getAllCountries() {
        return countryRepository.findAll();
    }

    @Override
    public CountryDto getCountryById(Long id) {
        return countryRepository.findById(id)
                .map(CountryMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Country not found"));
    }

    @Override
    public void deleteCountry(Long id) {
        countryRepository.deleteById(id);
    }

    @Override
    public Page<CountryDto> getAllCountries(Pageable pageable) {
        return countryRepository.findAll(pageable).map(CountryMapper::toDto);
    }
} 