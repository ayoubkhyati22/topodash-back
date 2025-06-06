package com.topographe.topographe.service;

import com.topographe.topographe.dto.request.ProjectCreateRequest;
import com.topographe.topographe.dto.request.ProjectUpdateRequest;
import com.topographe.topographe.dto.response.PageResponse;
import com.topographe.topographe.dto.response.ProjectResponse;
import com.topographe.topographe.entity.enumm.ProjectStatus;

import java.time.LocalDate;
import java.util.List;

public interface ProjectService {

    ProjectResponse createProject(ProjectCreateRequest request);

    PageResponse<ProjectResponse> getAllProjects(int page, int size, String sortBy, String sortDir);

    PageResponse<ProjectResponse> getProjectsWithFilters(
            int page, int size, String sortBy, String sortDir,
            ProjectStatus status, Long clientId, Long topographeId,
            LocalDate startDate, LocalDate endDate, String name
    );

    PageResponse<ProjectResponse> getProjectsByClient(
            Long clientId, int page, int size, String sortBy, String sortDir
    );

    PageResponse<ProjectResponse> getProjectsByTopographe(
            Long topographeId, int page, int size, String sortBy, String sortDir
    );

    ProjectResponse getProjectById(Long id);

    ProjectResponse updateProject(Long id, ProjectUpdateRequest request);

    void deleteProject(Long id);

    // Gestion des statuts
    ProjectResponse updateProjectStatus(Long id, ProjectStatus status);

    ProjectResponse startProject(Long id);

    ProjectResponse completeProject(Long id);

    ProjectResponse cancelProject(Long id);

    ProjectResponse putOnHold(Long id);

    // Méthodes utilitaires
    List<ProjectResponse> getOverdueProjects();

    List<ProjectResponse> getProjectsEndingSoon(int days);

    List<ProjectResponse> getActiveProjects();

    List<ProjectResponse> getProjectsByPeriod(LocalDate startDate, LocalDate endDate);

    // Méthodes statistiques
    long getTotalProjectsByStatus(ProjectStatus status);

    long getProjectCountByClient(Long clientId);

    long getProjectCountByTopographe(Long topographeId);
}