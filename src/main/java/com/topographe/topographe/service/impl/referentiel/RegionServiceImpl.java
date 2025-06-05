package com.topographe.topographe.service.impl.referentiel;

import com.topographe.topographe.dto.referentiel.RegionDto;
import com.topographe.topographe.mapper.referentiel.RegionMapper;
import com.topographe.topographe.repository.referentiel.RegionRepository;
import com.topographe.topographe.service.referentiel.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {
    private final RegionRepository regionRepository;

    @Override
    public RegionDto getRegionById(Long id) {
        return regionRepository.findById(id)
                .map(RegionMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Region not found"));
    }

    @Override
    public void deleteRegion(Long id) {
        regionRepository.deleteById(id);
    }

    @Override
    public Page<RegionDto> getAllRegions(Pageable pageable) {
        return regionRepository.findAll(pageable).map(RegionMapper::toDto);
    }
}
