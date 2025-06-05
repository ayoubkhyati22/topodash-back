package com.topographe.topographe.controller.referentiel;

import com.topographe.topographe.dto.referentiel.CityDto;
import com.topographe.topographe.dto.response.ApiResponse;
import com.topographe.topographe.dto.response.PageResponse;
import com.topographe.topographe.service.referentiel.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cities")
@RequiredArgsConstructor
public class CityController {
    private final CityService cityService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CityDto>> getCityById(@PathVariable Long id) {
        CityDto regionDto = cityService.getCityById(id);
        return ResponseEntity.ok(new ApiResponse<>("City found", regionDto, 200));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCity(@PathVariable Long id) {
        cityService.deleteCity(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/page/{page}/size/{size}")
    public ResponseEntity<ApiResponse<PageResponse<CityDto>>> getAllCitiesPaginated(
            @PathVariable int page,
            @PathVariable int size) {
        Page<CityDto> regionPage = cityService.getAllCities(PageRequest.of(page, size));
        PageResponse<CityDto> response = new PageResponse<>(
                regionPage.getContent(),
                regionPage.getTotalElements(),
                regionPage.getTotalPages(),
                regionPage.getNumber(),
                regionPage.getSize()
        );
        return ResponseEntity.ok(new ApiResponse<>("All cities fetched", response, HttpStatus.OK.value()));
    }
}
