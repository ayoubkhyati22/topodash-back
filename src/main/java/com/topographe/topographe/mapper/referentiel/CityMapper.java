package com.topographe.topographe.mapper.referentiel;

import com.topographe.topographe.dto.referentiel.CityDto;
import com.topographe.topographe.entity.referentiel.City;

public class CityMapper {
    public static CityDto toDto(City city) {
        if (city == null) return null;
        return CityDto.builder()
                .id(city.getId())
                .name(city.getName())
                .build();
    }

    public static City toEntity(CityDto dto) {
        if (dto == null) return null;
        return City.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }
}
