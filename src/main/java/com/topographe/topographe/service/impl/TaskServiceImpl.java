package com.topographe.topographe.service.impl;

import com.topographe.topographe.dto.request.TaskAssignRequest;
import com.topographe.topographe.dto.request.TaskCreateRequest;
import com.topographe.topographe.dto.request.TaskUpdateRequest;
import com.topographe.topographe.dto.response.PageResponse;
import com.topographe.topographe.dto.response.TaskResponse;
import com.topographe.topographe.entity.Project;
import com.topographe.topographe.entity.Task;
import com.topographe.topographe.entity.Technicien;
import com.topographe.topographe.entity.enumm.TaskStatus;
import com.topographe.topographe.exception.ResourceNotFoundException;
import com.topographe.topographe.mapper.TaskMapper;
import com.topographe.topographe.repository.ProjectRepository;
import com.topographe.topographe.repository.TaskRepository;
import com.topographe.topographe.repository.TechnicienRepository;
import com.topographe.topographe.service.TaskService;
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
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TechnicienRepository technicienRepository;
    private final TaskMapper taskMapper;

    @Override
    @Transactional
    public TaskResponse createTask(TaskCreateRequest request) {
        // Récupérer le projet
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Projet non trouvé avec l'ID: " + request.getProjectId()));

        // Vérifier que le projet est actif (pas COMPLETED ou CANCELLED)
        if (project.getStatus() == com.topographe.topographe.entity.enumm.ProjectStatus.COMPLETED ||
                project.getStatus() == com.topographe.topographe.entity.enumm.ProjectStatus.CANCELLED) {
            throw new IllegalStateException("Impossible de créer une tâche dans un projet terminé ou annulé");
        }

        // Récupérer le technicien assigné si spécifié
        Technicien assignedTechnicien = null;
        if (request.getAssignedTechnicienId() != null) {
            assignedTechnicien = technicienRepository.findById(request.getAssignedTechnicienId())
                    .orElseThrow(() -> new ResourceNotFoundException("Technicien non trouvé avec l'ID: " + request.getAssignedTechnicienId()));

            // Vérifier que le technicien est actif
            if (!assignedTechnicien.getIsActive()) {
                throw new IllegalStateException("Le technicien doit être actif pour être assigné à une tâche");
            }
        }

        // Valider la date d'échéance
        if (request.getDueDate() != null && project.getEndDate() != null &&
                request.getDueDate().isAfter(project.getEndDate())) {
            throw new IllegalArgumentException("La date d'échéance de la tâche ne peut pas dépasser la date de fin du projet");
        }

        // Créer la tâche
        Task task = taskMapper.toEntity(request, project, assignedTechnicien);
        Task savedTask = taskRepository.save(task);

        return taskMapper.toResponse(savedTask);
    }

    @Override
    public PageResponse<TaskResponse> getAllTasks(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Task> taskPage = taskRepository.findAll(pageable);

        return buildPageResponse(taskPage);
    }

    @Override
    public PageResponse<TaskResponse> getTasksWithFilters(
            int page, int size, String sortBy, String sortDir,
            TaskStatus status, Long projectId, Long technicienId, Long topographeId, Long clientId,
            LocalDate dueDateFrom, LocalDate dueDateTo, String title) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Task> taskPage = taskRepository.findWithFilters(
                status, projectId, technicienId, topographeId, clientId,
                dueDateFrom, dueDateTo, title, pageable);

        return buildPageResponse(taskPage);
    }

    @Override
    public PageResponse<TaskResponse> getTasksByProject(
            Long projectId, int page, int size, String sortBy, String sortDir) {

        // Vérifier que le projet existe
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Projet non trouvé avec l'ID: " + projectId);
        }

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Task> taskPage = taskRepository.findByProjectId(projectId, pageable);

        return buildPageResponse(taskPage);
    }

    @Override
    public PageResponse<TaskResponse> getTasksByTechnicien(
            Long technicienId, int page, int size, String sortBy, String sortDir) {

        // Vérifier que le technicien existe
        if (!technicienRepository.existsById(technicienId)) {
            throw new ResourceNotFoundException("Technicien non trouvé avec l'ID: " + technicienId);
        }

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Task> taskPage = taskRepository.findByAssignedTechnicienId(technicienId, pageable);

        return buildPageResponse(taskPage);
    }

    @Override
    public PageResponse<TaskResponse> getTasksByTopographe(
            Long topographeId, int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Task> taskPage = taskRepository.findByTopographeId(topographeId, pageable);

        return buildPageResponse(taskPage);
    }

    @Override
    public PageResponse<TaskResponse> getTasksByClient(
            Long clientId, int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Task> taskPage = taskRepository.findByClientId(clientId, pageable);

        return buildPageResponse(taskPage);
    }

    @Override
    public TaskResponse getTaskById(Long id) {
        Task task = findTaskById(id);
        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long id, TaskUpdateRequest request) {
        Task task = findTaskById(id);

        // Vérifier que la tâche n'est pas terminée
        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new IllegalStateException("Impossible de modifier une tâche terminée");
        }

        // Récupérer le technicien assigné si spécifié
        Technicien assignedTechnicien = null;
        if (request.getAssignedTechnicienId() != null) {
            assignedTechnicien = technicienRepository.findById(request.getAssignedTechnicienId())
                    .orElseThrow(() -> new ResourceNotFoundException("Technicien non trouvé avec l'ID: " + request.getAssignedTechnicienId()));

            if (!assignedTechnicien.getIsActive()) {
                throw new IllegalStateException("Le technicien doit être actif pour être assigné à une tâche");
            }
        }

        // Valider la transition de statut
        validateStatusTransition(task.getStatus(), request.getStatus());

        // Valider la date d'échéance
        if (request.getDueDate() != null && task.getProject().getEndDate() != null &&
                request.getDueDate().isAfter(task.getProject().getEndDate())) {
            throw new IllegalArgumentException("La date d'échéance de la tâche ne peut pas dépasser la date de fin du projet");
        }

        // Mettre à jour les champs
        taskMapper.updateEntity(task, request, assignedTechnicien);
        Task updatedTask = taskRepository.save(task);

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        Task task = findTaskById(id);

        // Une tâche ne peut être supprimée que si elle est en statut TODO
        if (task.getStatus() != TaskStatus.TODO) {
            throw new IllegalStateException("Seules les tâches en statut TODO peuvent être supprimées");
        }

        taskRepository.delete(task);
    }

    @Override
    @Transactional
    public TaskResponse assignTask(Long taskId, TaskAssignRequest request) {
        Task task = findTaskById(taskId);

        // Vérifier que la tâche n'est pas terminée
        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new IllegalStateException("Impossible d'assigner une tâche terminée");
        }

        Technicien technicien = technicienRepository.findById(request.getTechnicienId())
                .orElseThrow(() -> new ResourceNotFoundException("Technicien non trouvé avec l'ID: " + request.getTechnicienId()));

        if (!technicien.getIsActive()) {
            throw new IllegalStateException("Le technicien doit être actif pour être assigné à une tâche");
        }

        task.setAssignedTechnicien(technicien);
        Task updatedTask = taskRepository.save(task);

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    public TaskResponse unassignTask(Long taskId) {
        Task task = findTaskById(taskId);

        // Vérifier que la tâche n'est pas en cours ou terminée
        if (task.getStatus() == TaskStatus.IN_PROGRESS || task.getStatus() == TaskStatus.COMPLETED) {
            throw new IllegalStateException("Impossible de désassigner une tâche en cours ou terminée");
        }

        task.setAssignedTechnicien(null);
        // Remettre le statut à TODO si elle était en REVIEW
        if (task.getStatus() == TaskStatus.REVIEW) {
            task.setStatus(TaskStatus.TODO);
        }

        Task updatedTask = taskRepository.save(task);
        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    public TaskResponse reassignTask(Long taskId, Long newTechnicienId) {
        Task task = findTaskById(taskId);

        Technicien newTechnicien = technicienRepository.findById(newTechnicienId)
                .orElseThrow(() -> new ResourceNotFoundException("Technicien non trouvé avec l'ID: " + newTechnicienId));

        if (!newTechnicien.getIsActive()) {
            throw new IllegalStateException("Le nouveau technicien doit être actif");
        }

        task.setAssignedTechnicien(newTechnicien);
        Task updatedTask = taskRepository.save(task);

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    public TaskResponse updateTaskStatus(Long id, TaskStatus status) {
        Task task = findTaskById(id);

        // Vérifier la transition de statut
        validateStatusTransition(task.getStatus(), status);

        // Vérifications spécifiques selon le statut
        if (status == TaskStatus.IN_PROGRESS && task.getAssignedTechnicien() == null) {
            throw new IllegalStateException("Une tâche doit être assignée à un technicien avant de pouvoir être démarrée");
        }

        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    public TaskResponse startTask(Long id) {
        return updateTaskStatus(id, TaskStatus.IN_PROGRESS);
    }

    @Override
    @Transactional
    public TaskResponse completeTask(Long id) {
        return updateTaskStatus(id, TaskStatus.COMPLETED);
    }

    @Override
    @Transactional
    public TaskResponse putTaskInReview(Long id) {
        return updateTaskStatus(id, TaskStatus.REVIEW);
    }

    @Override
    @Transactional
    public TaskResponse resetTaskToTodo(Long id) {
        return updateTaskStatus(id, TaskStatus.TODO);
    }

    @Override
    public List<TaskResponse> getUnassignedTasks() {
        List<Task> unassignedTasks = taskRepository.findByAssignedTechnicienIsNull();
        return unassignedTasks.stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponse> getOverdueTasks() {
        List<Task> overdueTasks = taskRepository.findOverdueTasks(LocalDate.now());
        return overdueTasks.stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponse> getTasksDueSoon(int days) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);
        List<Task> tasksDueSoon = taskRepository.findTasksDueSoon(startDate, endDate);
        return tasksDueSoon.stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponse> getActiveTasks() {
        List<Task> activeTasks = taskRepository.findActiveTasks();
        return activeTasks.stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponse> getTasksByPriority(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Task> priorityTasks = taskRepository.findTasksByPriority(pageable);
        return priorityTasks.stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponse> getTasksCreatedInPeriod(LocalDate startDate, LocalDate endDate) {
        List<Task> tasks = taskRepository.findTasksCreatedInPeriod(startDate, endDate);
        return tasks.stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long getTotalTasksByStatus(TaskStatus status) {
        return taskRepository.countByStatus(status);
    }

    @Override
    public long getTaskCountByProject(Long projectId) {
        return taskRepository.countByProjectId(projectId);
    }

    @Override
    public long getTaskCountByTechnicien(Long technicienId) {
        return taskRepository.countByTechnicienId(technicienId);
    }

    @Override
    public long getTaskCountByTopographe(Long topographeId) {
        return taskRepository.countByTopographeId(topographeId);
    }

// Méthodes utilitaires

    private Task findTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tâche non trouvée avec l'ID: " + id));
    }

    private PageResponse<TaskResponse> buildPageResponse(Page<Task> taskPage) {
        List<TaskResponse> taskResponses = taskPage.getContent()
                .stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                taskResponses,
                taskPage.getNumber(),
                taskPage.getSize(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages(),
                taskPage.isFirst(),
                taskPage.isLast(),
                taskPage.hasNext(),
                taskPage.hasPrevious()
        );
    }

    private void validateStatusTransition(TaskStatus currentStatus, TaskStatus newStatus) {
        // Règles de transition des statuts
        switch (currentStatus) {
            case TODO:
                if (newStatus != TaskStatus.IN_PROGRESS && newStatus != TaskStatus.COMPLETED) {
                    throw new IllegalStateException("Une tâche TODO ne peut passer qu'à IN_PROGRESS ou COMPLETED");
                }
                break;
            case IN_PROGRESS:
                if (newStatus != TaskStatus.REVIEW && newStatus != TaskStatus.COMPLETED && newStatus != TaskStatus.TODO) {
                    throw new IllegalStateException("Une tâche IN_PROGRESS ne peut passer qu'à REVIEW, COMPLETED ou TODO");
                }
                break;
            case REVIEW:
                if (newStatus != TaskStatus.COMPLETED && newStatus != TaskStatus.IN_PROGRESS && newStatus != TaskStatus.TODO) {
                    throw new IllegalStateException("Une tâche REVIEW ne peut passer qu'à COMPLETED, IN_PROGRESS ou TODO");
                }
                break;
            case COMPLETED:
                throw new IllegalStateException("Une tâche terminée ne peut plus changer de statut");
        }
    }

}