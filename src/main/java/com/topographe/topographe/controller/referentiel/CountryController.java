package com.topographe.topographe.controller.referentiel;

import com.topographe.topographe.dto.response.ApiResponse;
import com.topographe.topographe.entity.referentiel.Country;
import com.topographe.topographe.service.referentiel.CountryService;
import com.topographe.topographe.dto.referentiel.CountryDto;
import com.topographe.topographe.mapper.referentiel.CountryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.topographe.topographe.dto.response.RefPageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/countries")
@RequiredArgsConstructor
public class CountryController {
    private final CountryService countryService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CountryDto> createCountry(@RequestBody CountryDto countryDto) {
        Country country = CountryMapper.toEntity(countryDto);
        Country saved = countryService.saveCountry(country);
        CountryDto responseDto = CountryMapper.toDto(saved);
        return ResponseEntity.ok(responseDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CountryDto>> getCountryById(@PathVariable Long id) {
        CountryDto countryDto = countryService.getCountryById(id);
        return ResponseEntity.ok(new ApiResponse<>("Country found", countryDto, 200));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCountry(@PathVariable Long id) {
        countryService.deleteCountry(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/page/{page}/size/{size}")
    public ResponseEntity<ApiResponse<RefPageResponse<CountryDto>>> getAllCountriesPaginated(
            @PathVariable int page,
            @PathVariable int size) {
        Page<CountryDto> countryPage = countryService.getAllCountries(PageRequest.of(page, size));
        RefPageResponse<CountryDto> response = new RefPageResponse<>(
            countryPage.getContent(),
            countryPage.getTotalElements(),
            countryPage.getTotalPages(),
            countryPage.getNumber(),
            countryPage.getSize()
        );
        return ResponseEntity.ok(new ApiResponse<>("All countries fetched", response, HttpStatus.OK.value()));
    }
} 