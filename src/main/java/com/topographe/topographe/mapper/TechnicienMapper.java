package com.topographe.topographe.mapper;

import com.topographe.topographe.dto.request.TechnicienCreateRequest;
import com.topographe.topographe.dto.request.TechnicienUpdateRequest;
import com.topographe.topographe.dto.response.TechnicienResponse;
import com.topographe.topographe.entity.Technicien;
import com.topographe.topographe.entity.Topographe;
import com.topographe.topographe.entity.enumm.Role;
import com.topographe.topographe.entity.referentiel.City;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class TechnicienMapper {

    public Technicien toEntity(TechnicienCreateRequest request, City city, Topographe assignedTo, String encodedPassword) {
        Technicien technicien = new Technicien();
        technicien.setUsername(request.getUsername());
        technicien.setEmail(request.getEmail());
        technicien.setPassword(encodedPassword);
        technicien.setPhoneNumber(request.getPhoneNumber());
        technicien.setFirstName(request.getFirstName());
        technicien.setLastName(request.getLastName());
        technicien.setBirthday(request.getBirthday());
        technicien.setCin(request.getCin());
        technicien.setCity(city);
        technicien.setRole(Role.TECHNICIEN);
        technicien.setSkillLevel(request.getSkillLevel());
        technicien.setSpecialties(request.getSpecialties());
        technicien.setAssignedTo(assignedTo);
        technicien.setIsActive(true);
        return technicien;
    }

    public void updateEntity(Technicien technicien, TechnicienUpdateRequest request, City city, Topographe assignedTo) {
        technicien.setEmail(request.getEmail());
        technicien.setPhoneNumber(request.getPhoneNumber());
        technicien.setFirstName(request.getFirstName());
        technicien.setLastName(request.getLastName());
        technicien.setBirthday(request.getBirthday());
        technicien.setCity(city);
        technicien.setSkillLevel(request.getSkillLevel());
        technicien.setSpecialties(request.getSpecialties()); // Corrigé : getSpecialties() au lieu de getSpecialities()
        technicien.setAssignedTo(assignedTo);
    }

    public TechnicienResponse toResponse(Technicien technicien) {
        TechnicienResponse response = new TechnicienResponse();
        response.setId(technicien.getId());
        response.setUsername(technicien.getUsername());
        response.setEmail(technicien.getEmail());
        response.setPhoneNumber(technicien.getPhoneNumber());
        response.setFirstName(technicien.getFirstName());
        response.setLastName(technicien.getLastName());
        response.setBirthday(technicien.getBirthday());
        response.setCin(technicien.getCin());
        response.setCityName(technicien.getCity().getName());
        response.setRole(technicien.getRole());
        response.setSkillLevel(technicien.getSkillLevel());
        response.setSpecialties(technicien.getSpecialties());
        response.setAssignedToTopographeName(
                technicien.getAssignedTo().getFirstName() + " " + technicien.getAssignedTo().getLastName()
        );
        response.setAssignedToTopographeId(technicien.getAssignedTo().getId());
        response.setCreatedAt(technicien.getCreatedAt());
        response.setIsActive(technicien.getIsActive());

        // Les statistiques seront calculées dans le service avec les requêtes du repository
        // pour éviter les problèmes de lazy loading
        response.setTotalTasks(0);
        response.setActiveTasks(0);
        response.setCompletedTasks(0);
        response.setTodoTasks(0);
        response.setReviewTasks(0);
        response.setTotalProjects(0);
        response.setActiveProjects(0);
        response.setCompletedProjects(0);
        response.setWorkloadPercentage(0.0);
        response.setAvailable(true);
        response.setMaxRecommendedTasks(5);
        response.setCompletionRate(0.0);
        response.setAverageTasksPerProject(0.0);

        return response;
    }
}