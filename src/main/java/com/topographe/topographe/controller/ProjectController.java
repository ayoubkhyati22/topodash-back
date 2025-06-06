package com.topographe.topographe.controller;

import com.topographe.topographe.dto.request.ProjectCreateRequest;
import com.topographe.topographe.dto.request.ProjectUpdateRequest;
import com.topographe.topographe.dto.response.ApiResponse;
import com.topographe.topographe.dto.response.PageResponse;
import com.topographe.topographe.dto.response.ProjectResponse;
import com.topographe.topographe.entity.enumm.ProjectStatus;
import com.topographe.topographe.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody ProjectCreateRequest request) {
        ProjectResponse projectResponse = projectService.createProject(request);
        ApiResponse<ProjectResponse> response = new ApiResponse<>(
                "Projet créé avec succès",
                projectResponse,
                HttpStatus.CREATED.value()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProjectResponse>>> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PageResponse<ProjectResponse> pageResponse = projectService.getAllProjects(page, size, sortBy, sortDir);
        ApiResponse<PageResponse<ProjectResponse>> response = new ApiResponse<>(
                "Liste des projets récupérée avec succès",
                pageResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ProjectResponse>>> searchProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long topographeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String name) {

        PageResponse<ProjectResponse> pageResponse = projectService.getProjectsWithFilters(
                page, size, sortBy, sortDir, status, clientId, topographeId, startDate, endDate, name);

        ApiResponse<PageResponse<ProjectResponse>> response = new ApiResponse<>(
                "Recherche de projets effectuée avec succès",
                pageResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<ApiResponse<PageResponse<ProjectResponse>>> getProjectsByClient(
            @PathVariable Long clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PageResponse<ProjectResponse> pageResponse = projectService.getProjectsByClient(
                clientId, page, size, sortBy, sortDir);

        ApiResponse<PageResponse<ProjectResponse>> response = new ApiResponse<>(
                "Projets du client récupérés avec succès",
                pageResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/topographe/{topographeId}")
    public ResponseEntity<ApiResponse<PageResponse<ProjectResponse>>> getProjectsByTopographe(
            @PathVariable Long topographeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PageResponse<ProjectResponse> pageResponse = projectService.getProjectsByTopographe(
                topographeId, page, size, sortBy, sortDir);

        ApiResponse<PageResponse<ProjectResponse>> response = new ApiResponse<>(
                "Projets du topographe récupérés avec succès",
                pageResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(@PathVariable Long id) {
        ProjectResponse projectResponse = projectService.getProjectById(id);
        ApiResponse<ProjectResponse> response = new ApiResponse<>(
                "Projet trouvé avec succès",
                projectResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectUpdateRequest request) {

        ProjectResponse projectResponse = projectService.updateProject(id, request);
        ApiResponse<ProjectResponse> response = new ApiResponse<>(
                "Projet mis à jour avec succès",
                projectResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        ApiResponse<String> response = new ApiResponse<>(
                "Projet supprimé avec succès",
                null,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    // Gestion des statuts

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProjectStatus(
            @PathVariable Long id,
            @RequestParam ProjectStatus status) {

        ProjectResponse projectResponse = projectService.updateProjectStatus(id, status);
        ApiResponse<ProjectResponse> response = new ApiResponse<>(
                "Statut du projet mis à jour avec succès",
                projectResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<ApiResponse<ProjectResponse>> startProject(@PathVariable Long id) {
        ProjectResponse projectResponse = projectService.startProject(id);
        ApiResponse<ProjectResponse> response = new ApiResponse<>(
                "Projet démarré avec succès",
                projectResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<ProjectResponse>> completeProject(@PathVariable Long id) {
        ProjectResponse projectResponse = projectService.completeProject(id);
        ApiResponse<ProjectResponse> response = new ApiResponse<>(
                "Projet terminé avec succès",
                projectResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<ProjectResponse>> cancelProject(@PathVariable Long id) {
        ProjectResponse projectResponse = projectService.cancelProject(id);
        ApiResponse<ProjectResponse> response = new ApiResponse<>(
                "Projet annulé avec succès",
                projectResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/hold")
    public ResponseEntity<ApiResponse<ProjectResponse>> putProjectOnHold(@PathVariable Long id) {
        ProjectResponse projectResponse = projectService.putOnHold(id);
        ApiResponse<ProjectResponse> response = new ApiResponse<>(
                "Projet mis en attente avec succès",
                projectResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    // Utilitaires et rapports

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getOverdueProjects() {
        List<ProjectResponse> overdueProjects = projectService.getOverdueProjects();
        ApiResponse<List<ProjectResponse>> response = new ApiResponse<>(
                "Projets en retard récupérés avec succès",
                overdueProjects,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ending-soon")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getProjectsEndingSoon(
            @RequestParam(defaultValue = "7") int days) {

        List<ProjectResponse> projectsEndingSoon = projectService.getProjectsEndingSoon(days);
        ApiResponse<List<ProjectResponse>> response = new ApiResponse<>(
                "Projets se terminant bientôt récupérés avec succès",
                projectsEndingSoon,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getActiveProjects() {
        List<ProjectResponse> activeProjects = projectService.getActiveProjects();
        ApiResponse<List<ProjectResponse>> response = new ApiResponse<>(
                "Projets actifs récupérés avec succès",
                activeProjects,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/period")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getProjectsByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<ProjectResponse> projects = projectService.getProjectsByPeriod(startDate, endDate);
        ApiResponse<List<ProjectResponse>> response = new ApiResponse<>(
                "Projets de la période récupérés avec succès",
                projects,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    // Statistiques

    @GetMapping("/stats/status/{status}")
    public ResponseEntity<ApiResponse<Long>> getProjectCountByStatus(@PathVariable ProjectStatus status) {
        long count = projectService.getTotalProjectsByStatus(status);
        ApiResponse<Long> response = new ApiResponse<>(
                "Nombre de projets par statut récupéré avec succès",
                count,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/client/{clientId}")
    public ResponseEntity<ApiResponse<Long>> getProjectCountByClient(@PathVariable Long clientId) {
        long count = projectService.getProjectCountByClient(clientId);
        ApiResponse<Long> response = new ApiResponse<>(
                "Nombre de projets du client récupéré avec succès",
                count,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/topographe/{topographeId}")
    public ResponseEntity<ApiResponse<Long>> getProjectCountByTopographe(@PathVariable Long topographeId) {
        long count = projectService.getProjectCountByTopographe(topographeId);
        ApiResponse<Long> response = new ApiResponse<>(
                "Nombre de projets du topographe récupéré avec succès",
                count,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }
}