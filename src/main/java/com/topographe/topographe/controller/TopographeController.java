package com.topographe.topographe.controller;

import com.topographe.topographe.dto.request.TopographeCreateRequest;
import com.topographe.topographe.dto.request.TopographeUpdateRequest;
import com.topographe.topographe.dto.response.ApiResponse;
import com.topographe.topographe.dto.response.UserPageResponse;
import com.topographe.topographe.dto.response.TopographeResponse;
import com.topographe.topographe.service.TopographeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/topographe")
@RequiredArgsConstructor
public class TopographeController {

    private final TopographeService topographeService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<TopographeResponse>> createTopographe(
            @Valid @RequestBody TopographeCreateRequest request) {
        TopographeResponse topographeResponse = topographeService.createTopographe(request);
        ApiResponse<TopographeResponse> response = new ApiResponse<>(
                "Topographe créé avec succès",
                topographeResponse,
                HttpStatus.CREATED.value()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<UserPageResponse<TopographeResponse>>> getAllTopographes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        UserPageResponse<TopographeResponse> pageResponse = topographeService.getAllTopographes(page, size, sortBy, sortDir);
        ApiResponse<UserPageResponse<TopographeResponse>> response = new ApiResponse<>(
                "Liste des topographes récupérée avec succès",
                pageResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<UserPageResponse<TopographeResponse>>> searchTopographes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String cityName,
            @RequestParam(required = false) Boolean isActive) {

        UserPageResponse<TopographeResponse> pageResponse = topographeService.getTopographesWithFilters(
                page, size, sortBy, sortDir, specialization, cityName, isActive);

        ApiResponse<UserPageResponse<TopographeResponse>> response = new ApiResponse<>(
                "Recherche de topographes effectuée avec succès",
                pageResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TopographeResponse>> getTopographeById(@PathVariable Long id) {
        TopographeResponse topographeResponse = topographeService.getTopographeById(id);
        ApiResponse<TopographeResponse> response = new ApiResponse<>(
                "Topographe trouvé avec succès",
                topographeResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TopographeResponse>> updateTopographe(
            @PathVariable Long id,
            @Valid @RequestBody TopographeUpdateRequest request) {

        TopographeResponse topographeResponse = topographeService.updateTopographe(id, request);
        ApiResponse<TopographeResponse> response = new ApiResponse<>(
                "Topographe mis à jour avec succès",
                topographeResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTopographe(@PathVariable Long id) {
        topographeService.deleteTopographe(id);
        ApiResponse<String> response = new ApiResponse<>(
                "Topographe supprimé avec succès",
                null,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<String>> activateTopographe(@PathVariable Long id) {
        topographeService.activateTopographe(id);
        ApiResponse<String> response = new ApiResponse<>(
                "Topographe activé avec succès",
                null,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateTopographe(@PathVariable Long id) {
        topographeService.deactivateTopographe(id);
        ApiResponse<String> response = new ApiResponse<>(
                "Topographe désactivé avec succès",
                null,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }
}