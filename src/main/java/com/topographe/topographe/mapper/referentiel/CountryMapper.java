package com.topographe.topographe.mapper.referentiel;

import com.topographe.topographe.dto.referentiel.CountryDto;
import com.topographe.topographe.entity.referentiel.Country;

public class CountryMapper {
    public static CountryDto toDto(Country country) {
        if (country == null) return null;
        return CountryDto.builder()
                .id(country.getId())
                .name(country.getName())
                .code(country.getCode())
                .build();
    }

    public static Country toEntity(CountryDto dto) {
        if (dto == null) return null;
        return Country.builder()
                .id(dto.getId())
                .name(dto.getName())
                .code(dto.getCode())
                .build();
    }
} 