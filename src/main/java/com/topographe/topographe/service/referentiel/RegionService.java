package com.topographe.topographe.service.referentiel;

import com.topographe.topographe.dto.referentiel.RegionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RegionService {
    RegionDto getRegionById(Long id);
    void deleteRegion(Long id);
    Page<RegionDto> getAllRegions(Pageable pageable);
}
