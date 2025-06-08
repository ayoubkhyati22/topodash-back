package com.topographe.topographe.controller;

import com.topographe.topographe.dto.request.TechnicienCreateRequest;
import com.topographe.topographe.dto.request.TechnicienUpdateRequest;
import com.topographe.topographe.dto.response.ApiResponse;
import com.topographe.topographe.dto.response.PageResponse;
import com.topographe.topographe.dto.response.TechnicienResponse;
import com.topographe.topographe.entity.enumm.SkillLevel;
import com.topographe.topographe.service.TechnicienService;
import com.topographe.topographe.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/technicien")
@RequiredArgsConstructor
@Slf4j
public class TechnicienController {

    private final TechnicienService technicienService;

    @PreAuthorize("hasRole('ADMIN') or hasRole('TOPOGRAPHE')")
    @PostMapping
    public ResponseEntity<ApiResponse<TechnicienResponse>> createTechnicien(
            @Valid @RequestBody TechnicienCreateRequest request,
            Authentication authentication) {

        log.info("Creating technicien with username: {}", request.getUsername());

        // Récupérer l'utilisateur connecté
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        TechnicienResponse technicienResponse = technicienService.createTechnicien(request, userDetails.getUser());
        ApiResponse<TechnicienResponse> response = new ApiResponse<>(
                "Technicien créé avec succès",
                technicienResponse,
                HttpStatus.CREATED.value()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TOPOGRAPHE')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TechnicienResponse>>> getAllTechniciens(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Authentication authentication) {

        log.info("Fetching all techniciens - page: {}, size: {}, sortBy: {}, sortDir: {}",
                page, size, sortBy, sortDir);

        // Récupérer l'utilisateur connecté
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        PageResponse<TechnicienResponse> pageResponse = technicienService.getAllTechniciens(
                page, size, sortBy, sortDir, userDetails.getUser());
        ApiResponse<PageResponse<TechnicienResponse>> response = new ApiResponse<>(
                "Liste des techniciens récupérée avec succès",
                pageResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TOPOGRAPHE')")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<TechnicienResponse>>> searchTechniciens(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String skillLevel,
            @RequestParam(required = false) String cityName,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Long topographeId,
            @RequestParam(required = false) String specialties,
            Authentication authentication) {

        log.info("Searching techniciens with filters - page: {}, size: {}, skillLevel: {}, cityName: {}, isActive: {}, topographeId: {}, specialties: {}",
                page, size, skillLevel, cityName, isActive, topographeId, specialties);

        try {
            // Récupérer l'utilisateur connecté
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // Convertir la chaîne skillLevel en enum si elle n'est pas null
            SkillLevel skillLevelEnum = null;
            if (skillLevel != null && !skillLevel.trim().isEmpty()) {
                try {
                    skillLevelEnum = SkillLevel.valueOf(skillLevel.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid skillLevel value: {}", skillLevel);
                    ApiResponse<PageResponse<TechnicienResponse>> errorResponse = new ApiResponse<>(
                            "Niveau de compétence invalide: " + skillLevel,
                            null,
                            HttpStatus.BAD_REQUEST.value()
                    );
                    return ResponseEntity.badRequest().body(errorResponse);
                }
            }

            // Nettoyer les paramètres de chaîne
            String cleanCityName = (cityName != null && !cityName.trim().isEmpty()) ? cityName.trim() : null;
            String cleanSpecialties = (specialties != null && !specialties.trim().isEmpty()) ? specialties.trim() : null;

            log.info("Processed search parameters - skillLevelEnum: {}, cleanCityName: {}, cleanSpecialties: {}",
                    skillLevelEnum, cleanCityName, cleanSpecialties);

            PageResponse<TechnicienResponse> pageResponse = technicienService.getTechniciensWithFilters(
                    page, size, sortBy, sortDir, skillLevelEnum, cleanCityName, isActive,
                    topographeId, cleanSpecialties, userDetails.getUser());

            ApiResponse<PageResponse<TechnicienResponse>> response = new ApiResponse<>(
                    "Recherche de techniciens effectuée avec succès",
                    pageResponse,
                    HttpStatus.OK.value()
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error during technicien search", e);
            ApiResponse<PageResponse<TechnicienResponse>> errorResponse = new ApiResponse<>(
                    "Erreur lors de la recherche: " + e.getMessage(),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TOPOGRAPHE')")
    @GetMapping("/topographe/{topographeId}")
    public ResponseEntity<ApiResponse<PageResponse<TechnicienResponse>>> getTechniciensByTopographe(
            @PathVariable Long topographeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Authentication authentication) {

        log.info("Fetching techniciens for topographe: {}", topographeId);

        // Récupérer l'utilisateur connecté
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        PageResponse<TechnicienResponse> pageResponse = technicienService.getTechniciensByTopographe(
                topographeId, page, size, sortBy, sortDir, userDetails.getUser());

        ApiResponse<PageResponse<TechnicienResponse>> response = new ApiResponse<>(
                "Techniciens du topographe récupérés avec succès",
                pageResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TOPOGRAPHE')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TechnicienResponse>> getTechnicienById(
            @PathVariable Long id,
            Authentication authentication) {

        log.info("Fetching technicien by ID: {}", id);

        // Récupérer l'utilisateur connecté
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        TechnicienResponse technicienResponse = technicienService.getTechnicienById(id, userDetails.getUser());
        ApiResponse<TechnicienResponse> response = new ApiResponse<>(
                "Technicien trouvé avec succès",
                technicienResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TOPOGRAPHE')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TechnicienResponse>> updateTechnicien(
            @PathVariable Long id,
            @Valid @RequestBody TechnicienUpdateRequest request,
            Authentication authentication) {

        log.info("Updating technicien with ID: {}", id);

        // Récupérer l'utilisateur connecté
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        TechnicienResponse technicienResponse = technicienService.updateTechnicien(id, request, userDetails.getUser());
        ApiResponse<TechnicienResponse> response = new ApiResponse<>(
                "Technicien mis à jour avec succès",
                technicienResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TOPOGRAPHE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTechnicien(
            @PathVariable Long id,
            Authentication authentication) {

        log.info("Deleting technicien with ID: {}", id);

        // Récupérer l'utilisateur connecté
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        technicienService.deleteTechnicien(id, userDetails.getUser());
        ApiResponse<String> response = new ApiResponse<>(
                "Technicien supprimé avec succès",
                null,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TOPOGRAPHE')")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<String>> activateTechnicien(
            @PathVariable Long id,
            Authentication authentication) {

        log.info("Activating technicien with ID: {}", id);

        // Récupérer l'utilisateur connecté
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        technicienService.activateTechnicien(id, userDetails.getUser());
        ApiResponse<String> response = new ApiResponse<>(
                "Technicien activé avec succès",
                null,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TOPOGRAPHE')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateTechnicien(
            @PathVariable Long id,
            Authentication authentication) {

        log.info("Deactivating technicien with ID: {}", id);

        // Récupérer l'utilisateur connecté
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        technicienService.deactivateTechnicien(id, userDetails.getUser());
        ApiResponse<String> response = new ApiResponse<>(
                "Technicien désactivé avec succès",
                null,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{technicienId}/reassign/{newTopographeId}")
    public ResponseEntity<ApiResponse<TechnicienResponse>> reassignTechnicien(
            @PathVariable Long technicienId,
            @PathVariable Long newTopographeId,
            Authentication authentication) {

        log.info("Reassigning technicien {} to topographe {}", technicienId, newTopographeId);

        // Récupérer l'utilisateur connecté
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        TechnicienResponse technicienResponse = technicienService.reassignTechnicien(
                technicienId, newTopographeId, userDetails.getUser());
        ApiResponse<TechnicienResponse> response = new ApiResponse<>(
                "Technicien réassigné avec succès",
                technicienResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TOPOGRAPHE')")
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<TechnicienResponse>>> getAvailableTechniciens(
            @RequestParam(defaultValue = "3") int maxTasks,
            Authentication authentication) {

        // Récupérer l'utilisateur connecté
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        List<TechnicienResponse> availableTechniciens = technicienService.getAvailableTechniciens(
                maxTasks, userDetails.getUser());
        ApiResponse<List<TechnicienResponse>> response = new ApiResponse<>(
                "Techniciens disponibles récupérés avec succès",
                availableTechniciens,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TOPOGRAPHE')")
    @GetMapping("/skill-level/{skillLevel}")
    public ResponseEntity<ApiResponse<List<TechnicienResponse>>> getTechniciensBySkillLevel(
            @PathVariable SkillLevel skillLevel,
            Authentication authentication) {

        // Récupérer l'utilisateur connecté
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        List<TechnicienResponse> techniciens = technicienService.getTechniciensBySkillLevel(
                skillLevel, userDetails.getUser());
        ApiResponse<List<TechnicienResponse>> response = new ApiResponse<>(
                "Techniciens du niveau de compétence récupérés avec succès",
                techniciens,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TOPOGRAPHE')")
    @GetMapping("/stats/total")
    public ResponseEntity<ApiResponse<Long>> getTotalActiveTechniciens(Authentication authentication) {
        // Récupérer l'utilisateur connecté
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        long total = technicienService.getTotalActiveTechniciens(userDetails.getUser());
        ApiResponse<Long> response = new ApiResponse<>(
                "Nombre total de techniciens actifs récupéré avec succès",
                total,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TOPOGRAPHE')")
    @GetMapping("/stats/topographe/{topographeId}")
    public ResponseEntity<ApiResponse<Long>> getTechnicienCountByTopographe(
            @PathVariable Long topographeId,
            Authentication authentication) {

        // Récupérer l'utilisateur connecté
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        long count = technicienService.getTechnicienCountByTopographe(topographeId, userDetails.getUser());
        ApiResponse<Long> response = new ApiResponse<>(
                "Nombre de techniciens du topographe récupéré avec succès",
                count,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }
}