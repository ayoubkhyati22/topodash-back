package com.topographe.topographe.service;

import com.topographe.topographe.dto.request.TaskAssignRequest;
import com.topographe.topographe.dto.request.TaskCreateRequest;
import com.topographe.topographe.dto.request.TaskUpdateRequest;
import com.topographe.topographe.dto.response.PageResponse;
import com.topographe.topographe.dto.response.TaskResponse;
import com.topographe.topographe.entity.enumm.TaskStatus;

import java.time.LocalDate;
import java.util.List;

public interface TaskService {

    TaskResponse createTask(TaskCreateRequest request);

    PageResponse<TaskResponse> getAllTasks(int page, int size, String sortBy, String sortDir);

    PageResponse<TaskResponse> getTasksWithFilters(
            int page, int size, String sortBy, String sortDir,
            TaskStatus status, Long projectId, Long technicienId, Long topographeId, Long clientId,
            LocalDate dueDateFrom, LocalDate dueDateTo, String title
    );

    PageResponse<TaskResponse> getTasksByProject(
            Long projectId, int page, int size, String sortBy, String sortDir
    );

    PageResponse<TaskResponse> getTasksByTechnicien(
            Long technicienId, int page, int size, String sortBy, String sortDir
    );

    PageResponse<TaskResponse> getTasksByTopographe(
            Long topographeId, int page, int size, String sortBy, String sortDir
    );

    PageResponse<TaskResponse> getTasksByClient(
            Long clientId, int page, int size, String sortBy, String sortDir
    );

    TaskResponse getTaskById(Long id);

    TaskResponse updateTask(Long id, TaskUpdateRequest request);

    void deleteTask(Long id);

    // Gestion des assignations
    TaskResponse assignTask(Long taskId, TaskAssignRequest request);

    TaskResponse unassignTask(Long taskId);

    TaskResponse reassignTask(Long taskId, Long newTechnicienId);

    // Gestion des statuts
    TaskResponse updateTaskStatus(Long id, TaskStatus status);

    TaskResponse startTask(Long id);

    TaskResponse completeTask(Long id);

    TaskResponse putTaskInReview(Long id);

    TaskResponse resetTaskToTodo(Long id);

    // Méthodes utilitaires
    List<TaskResponse> getUnassignedTasks();

    List<TaskResponse> getOverdueTasks();

    List<TaskResponse> getTasksDueSoon(int days);

    List<TaskResponse> getActiveTasks();

    List<TaskResponse> getTasksByPriority(int limit);

    List<TaskResponse> getTasksCreatedInPeriod(LocalDate startDate, LocalDate endDate);

    // Méthodes statistiques
    long getTotalTasksByStatus(TaskStatus status);

    long getTaskCountByProject(Long projectId);

    long getTaskCountByTechnicien(Long technicienId);

    long getTaskCountByTopographe(Long topographeId);
}