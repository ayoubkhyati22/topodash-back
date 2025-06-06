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
        if (project.getTasks() != null) {
            response.setTotalTasks(project.getTasks().size());
            response.setTodoTasks((int) project.getTasks().stream()
                    .filter(task -> task.getStatus() == TaskStatus.TODO)
                    .count());
            response.setInProgressTasks((int) project.getTasks().stream()
                    .filter(task -> task.getStatus() == TaskStatus.IN_PROGRESS)
                    .count());
            response.setReviewTasks((int) project.getTasks().stream()
                    .filter(task -> task.getStatus() == TaskStatus.REVIEW)
                    .count());
            response.setCompletedTasks((int) project.getTasks().stream()
                    .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                    .count());

            // Calcul du pourcentage de progression
            if (response.getTotalTasks() > 0) {
                double progress = ((double) response.getCompletedTasks() / response.getTotalTasks()) * 100;
                response.setProgressPercentage(Math.round(progress * 100.0) / 100.0);
            } else {
                response.setProgressPercentage(0.0);
            }
        } else {
            response.setTotalTasks(0);
            response.setTodoTasks(0);
            response.setInProgressTasks(0);
            response.setReviewTasks(0);
            response.setCompletedTasks(0);
            response.setProgressPercentage(0.0);
        }

        // Calcul des jours restants et du retard
        LocalDate today = LocalDate.now();
        response.setIsCompleted(project.getStatus() == com.topographe.topographe.entity.enumm.ProjectStatus.COMPLETED);

        if (project.getEndDate() != null) {
            long daysRemaining = ChronoUnit.DAYS.between(today, project.getEndDate());
            response.setDaysRemaining((int) daysRemaining);
            response.setIsOverdue(daysRemaining < 0 && !response.getIsCompleted());
        } else {
            response.setDaysRemaining(null);
            response.setIsOverdue(false);
        }

        return response;
    }
}