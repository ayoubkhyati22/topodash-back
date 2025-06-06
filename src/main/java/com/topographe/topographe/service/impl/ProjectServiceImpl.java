package com.topographe.topographe.service.impl;

import com.topographe.topographe.dto.request.ProjectCreateRequest;
import com.topographe.topographe.dto.request.ProjectUpdateRequest;
import com.topographe.topographe.dto.response.UserPageResponse;
import com.topographe.topographe.dto.response.ProjectResponse;
import com.topographe.topographe.entity.Client;
import com.topographe.topographe.entity.Project;
import com.topographe.topographe.entity.Topographe;
import com.topographe.topographe.entity.enumm.ProjectStatus;
import com.topographe.topographe.exception.ResourceNotFoundException;
import com.topographe.topographe.mapper.ProjectMapper;
import com.topographe.topographe.repository.ClientRepository;
import com.topographe.topographe.repository.ProjectRepository;
import com.topographe.topographe.repository.TopographeRepository;
import com.topographe.topographe.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ClientRepository clientRepository;
    private final TopographeRepository topographeRepository;
    private final ProjectMapper projectMapper;

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request) {
        // Récupérer le client
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + request.getClientId()));

        // Vérifier que le client est actif
        if (!client.getIsActive()) {
            throw new IllegalStateException("Le client doit être actif pour créer un projet");
        }

        // Récupérer le topographe
        Topographe topographe = topographeRepository.findById(request.getTopographeId())
                .orElseThrow(() -> new ResourceNotFoundException("Topographe non trouvé avec l'ID: " + request.getTopographeId()));

        // Vérifier que le topographe est actif
        if (!topographe.getIsActive()) {
            throw new IllegalStateException("Le topographe doit être actif pour gérer un projet");
        }

        // Valider les dates
        validateProjectDates(request.getStartDate(), request.getEndDate());

        // Créer le projet
        Project project = projectMapper.toEntity(request, client, topographe);
        Project savedProject = projectRepository.save(project);

        return projectMapper.toResponse(savedProject);
    }

    @Override
    public UserPageResponse<ProjectResponse> getAllProjects(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Project> projectPage = projectRepository.findAll(pageable);

        return buildPageResponse(projectPage);
    }

    @Override
    public UserPageResponse<ProjectResponse> getProjectsWithFilters(
            int page, int size, String sortBy, String sortDir,
            ProjectStatus status, Long clientId, Long topographeId,
            LocalDate startDate, LocalDate endDate, String name) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Project> projectPage = projectRepository.findWithFilters(
                status, clientId, topographeId, startDate, endDate, name, pageable);

        return buildPageResponse(projectPage);
    }

    @Override
    public UserPageResponse<ProjectResponse> getProjectsByClient(
            Long clientId, int page, int size, String sortBy, String sortDir) {

        // Vérifier que le client existe
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client non trouvé avec l'ID: " + clientId);
        }

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Project> projectPage = projectRepository.findByClientId(clientId, pageable);

        return buildPageResponse(projectPage);
    }

    @Override
    public UserPageResponse<ProjectResponse> getProjectsByTopographe(
            Long topographeId, int page, int size, String sortBy, String sortDir) {

        // Vérifier que le topographe existe
        if (!topographeRepository.existsById(topographeId)) {
            throw new ResourceNotFoundException("Topographe non trouvé avec l'ID: " + topographeId);
        }

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Project> projectPage = projectRepository.findByTopographeId(topographeId, pageable);

        return buildPageResponse(projectPage);
    }

    @Override
    public ProjectResponse getProjectById(Long id) {
        Project project = findProjectById(id);
        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long id, ProjectUpdateRequest request) {
        Project project = findProjectById(id);

        // Valider les dates
        validateProjectDates(request.getStartDate(), request.getEndDate());

        // Vérifier les transitions de statut
        validateStatusTransition(project.getStatus(), request.getStatus());

        // Mettre à jour les champs
        projectMapper.updateEntity(project, request);
        Project updatedProject = projectRepository.save(project);

        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        Project project = findProjectById(id);

        // Vérifier s'il a des tâches
        if (!project.getTasks().isEmpty()) {
            throw new IllegalStateException("Impossible de supprimer un projet qui a des tâches");
        }

        // Un projet ne peut être supprimé que s'il est en statut PLANNING ou CANCELLED
        if (project.getStatus() != ProjectStatus.PLANNING && project.getStatus() != ProjectStatus.CANCELLED) {
            throw new IllegalStateException("Seuls les projets en statut PLANNING ou CANCELLED peuvent être supprimés");
        }

        projectRepository.delete(project);
    }

    @Override
    @Transactional
    public ProjectResponse updateProjectStatus(Long id, ProjectStatus status) {
        Project project = findProjectById(id);

        // Vérifier la transition de statut
        validateStatusTransition(project.getStatus(), status);

        project.setStatus(status);
        Project updatedProject = projectRepository.save(project);

        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    public ProjectResponse startProject(Long id) {
        return updateProjectStatus(id, ProjectStatus.IN_PROGRESS);
    }

    @Override
    @Transactional
    public ProjectResponse completeProject(Long id) {
        Project project = findProjectById(id);

        // Vérifier que toutes les tâches sont terminées
        boolean hasIncompleteTasks = project.getTasks().stream()
                .anyMatch(task -> task.getStatus() != com.topographe.topographe.entity.enumm.TaskStatus.COMPLETED);

        if (hasIncompleteTasks) {
            throw new IllegalStateException("Toutes les tâches doivent être terminées avant de compléter le projet");
        }

        return updateProjectStatus(id, ProjectStatus.COMPLETED);
    }

    @Override
    @Transactional
    public ProjectResponse cancelProject(Long id) {
        return updateProjectStatus(id, ProjectStatus.CANCELLED);
    }

    @Override
    @Transactional
    public ProjectResponse putOnHold(Long id) {
        return updateProjectStatus(id, ProjectStatus.ON_HOLD);
    }

    @Override
    public List<ProjectResponse> getOverdueProjects() {
        List<Project> overdueProjects = projectRepository.findOverdueProjects(LocalDate.now());
        return overdueProjects.stream()
                .map(projectMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectResponse> getProjectsEndingSoon(int days) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);
        List<Project> projectsEndingSoon = projectRepository.findProjectsEndingSoon(startDate, endDate);
        return projectsEndingSoon.stream()
                .map(projectMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectResponse> getActiveProjects() {
        List<Project> activeProjects = projectRepository.findActiveProjects();
        return activeProjects.stream()
                .map(projectMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectResponse> getProjectsByPeriod(LocalDate startDate, LocalDate endDate) {
        List<Project> projects = projectRepository.findProjectsByPeriod(startDate, endDate);
        return projects.stream()
                .map(projectMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long getTotalProjectsByStatus(ProjectStatus status) {
        return projectRepository.countByStatus(status);
    }

    @Override
    public long getProjectCountByClient(Long clientId) {
        return projectRepository.countByClientId(clientId);
    }

    @Override
    public long getProjectCountByTopographe(Long topographeId) {
        return projectRepository.countByTopographeId(topographeId);
    }

    // Méthodes utilitaires

    private Project findProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projet non trouvé avec l'ID: " + id));
    }

    private UserPageResponse<ProjectResponse> buildPageResponse(Page<Project> projectPage) {
        List<ProjectResponse> projectResponses = projectPage.getContent()
                .stream()
                .map(projectMapper::toResponse)
                .collect(Collectors.toList());

        return new UserPageResponse<>(
                projectResponses,
                projectPage.getNumber(),
                projectPage.getSize(),
                projectPage.getTotalElements(),
                projectPage.getTotalPages(),
                projectPage.isFirst(),
                projectPage.isLast(),
                projectPage.hasNext(),
                projectPage.hasPrevious()
        );
    }

    private void validateProjectDates(LocalDate startDate, LocalDate endDate) {
        if (endDate != null && startDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("La date de fin ne peut pas être antérieure à la date de début");
        }
    }

    private void validateStatusTransition(ProjectStatus currentStatus, ProjectStatus newStatus) {
        // Règles de transition des statuts
        switch (currentStatus) {
            case PLANNING:
                if (newStatus != ProjectStatus.IN_PROGRESS && newStatus != ProjectStatus.CANCELLED) {
                    throw new IllegalStateException("Un projet en PLANNING ne peut passer qu'à IN_PROGRESS ou CANCELLED");
                }
                break;
            case IN_PROGRESS:
                if (newStatus != ProjectStatus.ON_HOLD && newStatus != ProjectStatus.COMPLETED && newStatus != ProjectStatus.CANCELLED) {
                    throw new IllegalStateException("Un projet en IN_PROGRESS ne peut passer qu'à ON_HOLD, COMPLETED ou CANCELLED");
                }
                break;
            case ON_HOLD:
                if (newStatus != ProjectStatus.IN_PROGRESS && newStatus != ProjectStatus.CANCELLED) {
                    throw new IllegalStateException("Un projet en ON_HOLD ne peut passer qu'à IN_PROGRESS ou CANCELLED");
                }
                break;
            case COMPLETED:
            case CANCELLED:
                throw new IllegalStateException("Un projet terminé ou annulé ne peut plus changer de statut");
        }
    }
}