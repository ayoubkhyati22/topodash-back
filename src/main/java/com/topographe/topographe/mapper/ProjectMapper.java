package com.topographe.topographe.mapper;

import com.topographe.topographe.dto.request.ProjectCreateRequest;
import com.topographe.topographe.dto.request.ProjectUpdateRequest;
import com.topographe.topographe.dto.response.ProjectResponse;
import com.topographe.topographe.entity.Client;
import com.topographe.topographe.entity.Project;
import com.topographe.topographe.entity.Topographe;
import com.topographe.topographe.entity.enumm.TaskStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Component
public class ProjectMapper {

    public Project toEntity(ProjectCreateRequest request, Client client, Topographe topographe) {
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setClient(client);
        project.setTopographe(topographe);
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setStatus(request.getStatus());
        return project;
    }

    public void updateEntity(Project project, ProjectUpdateRequest request) {
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setStatus(request.getStatus());
    }

    public ProjectResponse toResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        response.setStatus(project.getStatus());
        response.setStartDate(project.getStartDate());
        response.setEndDate(project.getEndDate());
        response.setCreatedAt(project.getCreatedAt());

        // Informations du client
        Client client = project.getClient();
        response.setClientId(client.getId());
        response.setClientName(client.getFirstName() + " " + client.getLastName());
        response.setClientEmail(client.getEmail());
        response.setClientType(client.getClientType().name());
        response.setClientCompanyName(client.getCompanyName());

        // Informations du topographe
        Topographe topographe = project.getTopographe();
        response.setTopographeId(topographe.getId());
        response.setTopographeName(topographe.getFirstName() + " " + topographe.getLastName());
        response.setTopographeEmail(topographe.getEmail());
        response.setTopographeLicenseNumber(topographe.getLicenseNumber());

        // Statistiques des tâches
        response.setTotalTasks(project.getTotalTasksCount());
        response.setTodoTasks((int) project.getTodoTasksCount());
        response.setInProgressTasks((int) project.getInProgressTasksCount());
        response.setReviewTasks((int) project.getReviewTasksCount());
        response.setCompletedTasks((int) project.getCompletedTasksCount());

        // Calcul des pourcentages de progression
        response.setProgressPercentage(project.getProgressPercentage());
        response.setWeightedProgressPercentage(project.getWeightedProgressPercentage());

        // Informations temporelles
        LocalDate today = LocalDate.now();
        response.setIsCompleted(project.isCompleted());

        if (project.getEndDate() != null) {
            Long daysRemaining = project.getDaysRemaining();
            response.setDaysRemaining(daysRemaining != null ? daysRemaining.intValue() : null);
            response.setIsOverdue(project.isOverdue());
        } else {
            response.setDaysRemaining(null);
            response.setIsOverdue(false);
        }

        // Informations sur les techniciens
        response.setAssignedTechniciensCount(project.getAssignedTechniciensCount());
        response.setAssignedTechniciensNames(
                project.getAssignedTechniciens().stream()
                        .map(tech -> tech.getFirstName() + " " + tech.getLastName())
                        .collect(Collectors.joining(", "))
        );

        // Calcul des durées
        if (project.getStartDate() != null) {
            if (project.getEndDate() != null) {
                int totalDuration = (int) ChronoUnit.DAYS.between(project.getStartDate(), project.getEndDate());
                response.setTotalDuration(totalDuration);

                int elapsedDuration = (int) ChronoUnit.DAYS.between(project.getStartDate(), today);
                response.setElapsedDuration(Math.max(0, elapsedDuration));

                if (totalDuration > 0) {
                    double timeProgress = Math.min(((double) elapsedDuration / totalDuration) * 100, 100);
                    response.setTimeProgressPercentage(Math.round(timeProgress * 100.0) / 100.0);
                }
            }
        }

        // Évaluation de la santé du projet
        evaluateProjectHealth(response);

        return response;
    }

    private void evaluateProjectHealth(ProjectResponse response) {
        // Logique d'évaluation de la santé du projet
        double progressPercentage = response.getProgressPercentage() != null ? response.getProgressPercentage() : 0;
        double timeProgressPercentage = response.getTimeProgressPercentage() != null ? response.getTimeProgressPercentage() : 0;

        if (response.getIsOverdue()) {
            response.setHealthStatus("CRITICAL");
            response.setHealthMessage("Projet en retard - Action immédiate requise");
        } else if (timeProgressPercentage > progressPercentage + 20) {
            response.setHealthStatus("WARNING");
            response.setHealthMessage("Retard détecté - Surveillance recommandée");
        } else if (progressPercentage >= 90) {
            response.setHealthStatus("GOOD");
            response.setHealthMessage("Projet en bonne voie de finalisation");
        } else if (timeProgressPercentage <= progressPercentage + 10) {
            response.setHealthStatus("GOOD");
            response.setHealthMessage("Projet dans les temps");
        } else {
            response.setHealthStatus("WARNING");
            response.setHealthMessage("Progression légèrement en retard");
        }
    }
}