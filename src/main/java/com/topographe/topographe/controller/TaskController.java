package com.topographe.topographe.controller;

import com.topographe.topographe.dto.request.TaskAssignRequest;
import com.topographe.topographe.dto.request.TaskCreateRequest;
import com.topographe.topographe.dto.request.TaskUpdateRequest;
import com.topographe.topographe.dto.response.ApiResponse;
import com.topographe.topographe.dto.response.PageResponse;
import com.topographe.topographe.dto.response.TaskResponse;
import com.topographe.topographe.entity.enumm.TaskStatus;
import com.topographe.topographe.service.TaskService;
import com.topographe.topographe.service.impl.TaskServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskServiceImpl taskServiceImpl; // Pour accéder aux nouvelles méthodes

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody TaskCreateRequest request) {
        TaskResponse taskResponse = taskService.createTask(request);
        ApiResponse<TaskResponse> response = new ApiResponse<>(
                "Tâche créée avec succès",
                taskResponse,
                HttpStatus.CREATED.value()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TaskResponse>>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PageResponse<TaskResponse> pageResponse = taskService.getAllTasks(page, size, sortBy, sortDir);
        ApiResponse<PageResponse<TaskResponse>> response = new ApiResponse<>(
                "Liste des tâches récupérée avec succès",
                pageResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<TaskResponse>>> searchTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long technicienId,
            @RequestParam(required = false) Long topographeId,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateTo,
            @RequestParam(required = false) String title) {

        PageResponse<TaskResponse> pageResponse = taskService.getTasksWithFilters(
                page, size, sortBy, sortDir, status, projectId, technicienId,
                topographeId, clientId, dueDateFrom, dueDateTo, title);

        ApiResponse<PageResponse<TaskResponse>> response = new ApiResponse<>(
                "Recherche de tâches effectuée avec succès",
                pageResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<PageResponse<TaskResponse>>> getTasksByProject(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PageResponse<TaskResponse> pageResponse = taskService.getTasksByProject(
                projectId, page, size, sortBy, sortDir);

        ApiResponse<PageResponse<TaskResponse>> response = new ApiResponse<>(
                "Tâches du projet récupérées avec succès",
                pageResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/technicien/{technicienId}")
    public ResponseEntity<ApiResponse<PageResponse<TaskResponse>>> getTasksByTechnicien(
            @PathVariable Long technicienId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PageResponse<TaskResponse> pageResponse = taskService.getTasksByTechnicien(
                technicienId, page, size, sortBy, sortDir);

        ApiResponse<PageResponse<TaskResponse>> response = new ApiResponse<>(
                "Tâches du technicien récupérées avec succès",
                pageResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/topographe/{topographeId}")
    public ResponseEntity<ApiResponse<PageResponse<TaskResponse>>> getTasksByTopographe(
            @PathVariable Long topographeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PageResponse<TaskResponse> pageResponse = taskService.getTasksByTopographe(
                topographeId, page, size, sortBy, sortDir);

        ApiResponse<PageResponse<TaskResponse>> response = new ApiResponse<>(
                "Tâches du topographe récupérées avec succès",
                pageResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<ApiResponse<PageResponse<TaskResponse>>> getTasksByClient(
            @PathVariable Long clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PageResponse<TaskResponse> pageResponse = taskService.getTasksByClient(
                clientId, page, size, sortBy, sortDir);

        ApiResponse<PageResponse<TaskResponse>> response = new ApiResponse<>(
                "Tâches du client récupérées avec succès",
                pageResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(@PathVariable Long id) {
        TaskResponse taskResponse = taskService.getTaskById(id);
        ApiResponse<TaskResponse> response = new ApiResponse<>(
                "Tâche trouvée avec succès",
                taskResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskUpdateRequest request) {

        TaskResponse taskResponse = taskService.updateTask(id, request);
        ApiResponse<TaskResponse> response = new ApiResponse<>(
                "Tâche mise à jour avec succès",
                taskResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        ApiResponse<String> response = new ApiResponse<>(
                "Tâche supprimée avec succès",
                null,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    // Gestion des assignations multiples

    @PostMapping("/{taskId}/assign")
    public ResponseEntity<ApiResponse<TaskResponse>> assignTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskAssignRequest request) {

        TaskResponse taskResponse = taskService.assignTask(taskId, request);
        ApiResponse<TaskResponse> response = new ApiResponse<>(
                "Techniciens assignés avec succès à la tâche",
                taskResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{taskId}/assign")
    public ResponseEntity<ApiResponse<TaskResponse>> unassignTask(@PathVariable Long taskId) {
        TaskResponse taskResponse = taskService.unassignTask(taskId);
        ApiResponse<TaskResponse> response = new ApiResponse<>(
                "Tous les techniciens désassignés de la tâche",
                taskResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{taskId}/assign/{technicienId}")
    public ResponseEntity<ApiResponse<TaskResponse>> addTechnicienToTask(
            @PathVariable Long taskId,
            @PathVariable Long technicienId) {

        TaskResponse taskResponse = taskServiceImpl.addTechnicienToTask(taskId, technicienId);
        ApiResponse<TaskResponse> response = new ApiResponse<>(
                "Technicien ajouté à la tâche avec succès",
                taskResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{taskId}/assign/{technicienId}")
    public ResponseEntity<ApiResponse<TaskResponse>> removeTechnicienFromTask(
            @PathVariable Long taskId,
            @PathVariable Long technicienId) {

        TaskResponse taskResponse = taskServiceImpl.removeTechnicienFromTask(taskId, technicienId);
        ApiResponse<TaskResponse> response = new ApiResponse<>(
                "Technicien retiré de la tâche avec succès",
                taskResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{taskId}/reassign/{newTechnicienId}")
    public ResponseEntity<ApiResponse<TaskResponse>> reassignTask(
            @PathVariable Long taskId,
            @PathVariable Long newTechnicienId) {

        TaskResponse taskResponse = taskService.reassignTask(taskId, newTechnicienId);
        ApiResponse<TaskResponse> response = new ApiResponse<>(
                "Tâche réassignée avec succès",
                taskResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    // Gestion des statuts

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
            @PathVariable Long id,
            @RequestParam TaskStatus status) {

        TaskResponse taskResponse = taskService.updateTaskStatus(id, status);
        ApiResponse<TaskResponse> response = new ApiResponse<>(
                "Statut de la tâche mis à jour avec succès",
                taskResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<ApiResponse<TaskResponse>> startTask(@PathVariable Long id) {
        TaskResponse taskResponse = taskService.startTask(id);
        ApiResponse<TaskResponse> response = new ApiResponse<>(
                "Tâche démarrée avec succès",
                taskResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<TaskResponse>> completeTask(@PathVariable Long id) {
        TaskResponse taskResponse = taskService.completeTask(id);
        ApiResponse<TaskResponse> response = new ApiResponse<>(
                "Tâche terminée avec succès",
                taskResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/review")
    public ResponseEntity<ApiResponse<TaskResponse>> putTaskInReview(@PathVariable Long id) {
        TaskResponse taskResponse = taskService.putTaskInReview(id);
        ApiResponse<TaskResponse> response = new ApiResponse<>(
                "Tâche mise en révision avec succès",
                taskResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/reset")
    public ResponseEntity<ApiResponse<TaskResponse>> resetTaskToTodo(@PathVariable Long id) {
        TaskResponse taskResponse = taskService.resetTaskToTodo(id);
        ApiResponse<TaskResponse> response = new ApiResponse<>(
                "Tâche remise à TODO avec succès",
                taskResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    // Gestion du pourcentage de progression

    @PatchMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskProgress(
            @PathVariable Long id,
            @RequestParam Integer progressPercentage,
            @RequestParam(required = false) String progressNotes) {

        TaskResponse taskResponse = taskServiceImpl.updateTaskProgress(id, progressPercentage, progressNotes);
        ApiResponse<TaskResponse> response = new ApiResponse<>(
                "Progression de la tâche mise à jour avec succès",
                taskResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    // Utilitaires et rapports

    @GetMapping("/unassigned")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getUnassignedTasks() {
        List<TaskResponse> unassignedTasks = taskService.getUnassignedTasks();
        ApiResponse<List<TaskResponse>> response = new ApiResponse<>(
                "Tâches non assignées récupérées avec succès",
                unassignedTasks,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getOverdueTasks() {
        List<TaskResponse> overdueTasks = taskService.getOverdueTasks();
        ApiResponse<List<TaskResponse>> response = new ApiResponse<>(
                "Tâches en retard récupérées avec succès",
                overdueTasks,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/due-soon")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksDueSoon(
            @RequestParam(defaultValue = "3") int days) {

        List<TaskResponse> tasksDueSoon = taskService.getTasksDueSoon(days);
        ApiResponse<List<TaskResponse>> response = new ApiResponse<>(
                "Tâches à échéance proche récupérées avec succès",
                tasksDueSoon,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getActiveTasks() {
        List<TaskResponse> activeTasks = taskService.getActiveTasks();
        ApiResponse<List<TaskResponse>> response = new ApiResponse<>(
                "Tâches actives récupérées avec succès",
                activeTasks,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/priority")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByPriority(
            @RequestParam(defaultValue = "10") int limit) {

        List<TaskResponse> priorityTasks = taskService.getTasksByPriority(limit);
        ApiResponse<List<TaskResponse>> response = new ApiResponse<>(
                "Tâches prioritaires récupérées avec succès",
                priorityTasks,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/period")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksCreatedInPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<TaskResponse> tasks = taskService.getTasksCreatedInPeriod(startDate, endDate);
        ApiResponse<List<TaskResponse>> response = new ApiResponse<>(
                "Tâches de la période récupérées avec succès",
                tasks,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    // Statistiques détaillées

    @GetMapping("/stats/status/{status}")
    public ResponseEntity<ApiResponse<Long>> getTaskCountByStatus(@PathVariable TaskStatus status) {
        long count = taskService.getTotalTasksByStatus(status);
        ApiResponse<Long> response = new ApiResponse<>(
                "Nombre de tâches par statut récupéré avec succès",
                count,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/project/{projectId}")
    public ResponseEntity<ApiResponse<Long>> getTaskCountByProject(@PathVariable Long projectId) {
        long count = taskService.getTaskCountByProject(projectId);
        ApiResponse<Long> response = new ApiResponse<>(
                "Nombre de tâches du projet récupéré avec succès",
                count,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/technicien/{technicienId}")
    public ResponseEntity<ApiResponse<Long>> getTaskCountByTechnicien(@PathVariable Long technicienId) {
        long count = taskService.getTaskCountByTechnicien(technicienId);
        ApiResponse<Long> response = new ApiResponse<>(
                "Nombre de tâches du technicien récupéré avec succès",
                count,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/topographe/{topographeId}")
    public ResponseEntity<ApiResponse<Long>> getTaskCountByTopographe(@PathVariable Long topographeId) {
        long count = taskService.getTaskCountByTopographe(topographeId);
        ApiResponse<Long> response = new ApiResponse<>(
                "Nombre de tâches du topographe récupéré avec succès",
                count,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    // Nouvelles statistiques pour le dashboard

    @GetMapping("/stats/workload")
    public ResponseEntity<ApiResponse<List<Object[]>>> getWorkloadByTechnicien() {
        // Cette méthode devrait être implémentée dans le service
        ApiResponse<List<Object[]>> response = new ApiResponse<>(
                "Charge de travail par technicien récupérée avec succès",
                null, // À implémenter
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/completion-rate")
    public ResponseEntity<ApiResponse<Object>> getCompletionRate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Calcul du taux de completion sur une période
        ApiResponse<Object> response = new ApiResponse<>(
                "Taux de completion calculé avec succès",
                null, // À implémenter
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/progress-summary")
    public ResponseEntity<ApiResponse<Object>> getProgressSummary() {
        // Résumé de la progression de toutes les tâches
        ApiResponse<Object> response = new ApiResponse<>(
                "Résumé de progression récupéré avec succès",
                null, // À implémenter
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }
}