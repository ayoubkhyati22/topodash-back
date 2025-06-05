package com.topographe.topographe.mapper.referentiel;

import com.topographe.topographe.dto.referentiel.RegionDto;
import com.topographe.topographe.entity.referentiel.Region;

public class RegionMapper {
    public static RegionDto toDto(Region region) {
        if (region == null) return null;
        return RegionDto.builder()
                .id(region.getId())
                .name(region.getName())
                .build();
    }

    public static Region toEntity(RegionDto dto) {
        if (dto == null) return null;
        return Region.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }
}