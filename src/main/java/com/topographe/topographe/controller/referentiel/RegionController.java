package com.topographe.topographe.controller.referentiel;

import com.topographe.topographe.dto.referentiel.RegionDto;
import com.topographe.topographe.dto.response.ApiResponse;
import com.topographe.topographe.dto.response.RefPageResponse;
import com.topographe.topographe.service.referentiel.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/regions")
@RequiredArgsConstructor
public class RegionController {
    private final RegionService regionService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RegionDto>> getCountryById(@PathVariable Long id) {
        RegionDto regionDto = regionService.getRegionById(id);
        return ResponseEntity.ok(new ApiResponse<>("Region found", regionDto, 200));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRegion(@PathVariable Long id) {
        regionService.deleteRegion(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/page/{page}/size/{size}")
    public ResponseEntity<ApiResponse<RefPageResponse<RegionDto>>> getAllRegionsPaginated(
            @PathVariable int page,
            @PathVariable int size) {
        Page<RegionDto> regionPage = regionService.getAllRegions(PageRequest.of(page, size));
        RefPageResponse<RegionDto> response = new RefPageResponse<>(
                regionPage.getContent(),
                regionPage.getTotalElements(),
                regionPage.getTotalPages(),
                regionPage.getNumber(),
                regionPage.getSize()
        );
        return ResponseEntity.ok(new ApiResponse<>("All regions fetched", response, HttpStatus.OK.value()));
    }
}
