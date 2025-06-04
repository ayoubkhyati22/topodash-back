package com.topographe.topographe.mapper;

import com.topographe.topographe.dto.CountryDto;
import com.topographe.topographe.entity.Country;

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