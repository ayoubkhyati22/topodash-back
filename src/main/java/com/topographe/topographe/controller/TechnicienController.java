package com.topographe.topographe.controller;

import com.topographe.topographe.dto.request.TechnicienCreateRequest;
import com.topographe.topographe.dto.request.TechnicienUpdateRequest;
import com.topographe.topographe.dto.response.ApiResponse;
import com.topographe.topographe.dto.response.PageResponse;
import com.topographe.topographe.dto.response.TechnicienResponse;
import com.topographe.topographe.entity.enumm.SkillLevel;
import com.topographe.topographe.service.TechnicienService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/technicien")
@RequiredArgsConstructor
public class TechnicienController {

    private final TechnicienService technicienService;

    @PostMapping
    public ResponseEntity<ApiResponse<TechnicienResponse>> createTechnicien(
            @Valid @RequestBody TechnicienCreateRequest request) {
        TechnicienResponse technicienResponse = technicienService.createTechnicien(request);
        ApiResponse<TechnicienResponse> response = new ApiResponse<>(
                "Technicien créé avec succès",
                technicienResponse,
                HttpStatus.CREATED.value()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TechnicienResponse>>> getAllTechniciens(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PageResponse<TechnicienResponse> pageResponse = technicienService.getAllTechniciens(page, size, sortBy, sortDir);
        ApiResponse<PageResponse<TechnicienResponse>> response = new ApiResponse<>(
                "Liste des techniciens récupérée avec succès",
                pageResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<TechnicienResponse>>> searchTechniciens(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) SkillLevel skillLevel,
            @RequestParam(required = false) String cityName,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Long topographeId,
            @RequestParam(required = false) String specialties) {

        PageResponse<TechnicienResponse> pageResponse = technicienService.getTechniciensWithFilters(
                page, size, sortBy, sortDir, skillLevel, cityName, isActive, topographeId, specialties);

        ApiResponse<PageResponse<TechnicienResponse>> response = new ApiResponse<>(
                "Recherche de techniciens effectuée avec succès",
                pageResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/topographe/{topographeId}")
    public ResponseEntity<ApiResponse<PageResponse<TechnicienResponse>>> getTechniciensByTopographe(
            @PathVariable Long topographeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PageResponse<TechnicienResponse> pageResponse = technicienService.getTechniciensByTopographe(
                topographeId, page, size, sortBy, sortDir);

        ApiResponse<PageResponse<TechnicienResponse>> response = new ApiResponse<>(
                "Techniciens du topographe récupérés avec succès",
                pageResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TechnicienResponse>> getTechnicienById(@PathVariable Long id) {
        TechnicienResponse technicienResponse = technicienService.getTechnicienById(id);
        ApiResponse<TechnicienResponse> response = new ApiResponse<>(
                "Technicien trouvé avec succès",
                technicienResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TechnicienResponse>> updateTechnicien(
            @PathVariable Long id,
            @Valid @RequestBody TechnicienUpdateRequest request) {

        TechnicienResponse technicienResponse = technicienService.updateTechnicien(id, request);
        ApiResponse<TechnicienResponse> response = new ApiResponse<>(
                "Technicien mis à jour avec succès",
                technicienResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTechnicien(@PathVariable Long id) {
        technicienService.deleteTechnicien(id);
        ApiResponse<String> response = new ApiResponse<>(
                "Technicien supprimé avec succès",
                null,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<String>> activateTechnicien(@PathVariable Long id) {
        technicienService.activateTechnicien(id);
        ApiResponse<String> response = new ApiResponse<>(
                "Technicien activé avec succès",
                null,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateTechnicien(@PathVariable Long id) {
        technicienService.deactivateTechnicien(id);
        ApiResponse<String> response = new ApiResponse<>(
                "Technicien désactivé avec succès",
                null,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{technicienId}/reassign/{newTopographeId}")
    public ResponseEntity<ApiResponse<TechnicienResponse>> reassignTechnicien(
            @PathVariable Long technicienId,
            @PathVariable Long newTopographeId) {

        TechnicienResponse technicienResponse = technicienService.reassignTechnicien(technicienId, newTopographeId);
        ApiResponse<TechnicienResponse> response = new ApiResponse<>(
                "Technicien réassigné avec succès",
                technicienResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<TechnicienResponse>>> getAvailableTechniciens(
            @RequestParam(defaultValue = "3") int maxTasks) {

        List<TechnicienResponse> availableTechniciens = technicienService.getAvailableTechniciens(maxTasks);
        ApiResponse<List<TechnicienResponse>> response = new ApiResponse<>(
                "Techniciens disponibles récupérés avec succès",
                availableTechniciens,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/skill-level/{skillLevel}")
    public ResponseEntity<ApiResponse<List<TechnicienResponse>>> getTechniciensBySkillLevel(
            @PathVariable SkillLevel skillLevel) {

        List<TechnicienResponse> techniciens = technicienService.getTechniciensBySkillLevel(skillLevel);
        ApiResponse<List<TechnicienResponse>> response = new ApiResponse<>(
                "Techniciens du niveau de compétence récupérés avec succès",
                techniciens,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/total")
    public ResponseEntity<ApiResponse<Long>> getTotalActiveTechniciens() {
        long total = technicienService.getTotalActiveTechniciens();
        ApiResponse<Long> response = new ApiResponse<>(
                "Nombre total de techniciens actifs récupéré avec succès",
                total,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/topographe/{topographeId}")
    public ResponseEntity<ApiResponse<Long>> getTechnicienCountByTopographe(@PathVariable Long topographeId) {
        long count = technicienService.getTechnicienCountByTopographe(topographeId);
        ApiResponse<Long> response = new ApiResponse<>(
                "Nombre de techniciens du topographe récupéré avec succès",
                count,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }
}