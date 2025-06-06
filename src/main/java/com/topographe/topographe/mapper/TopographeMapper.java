package com.topographe.topographe.mapper;

import com.topographe.topographe.dto.request.TopographeCreateRequest;
import com.topographe.topographe.dto.request.TopographeUpdateRequest;
import com.topographe.topographe.dto.response.TopographeResponse;
import com.topographe.topographe.entity.Topographe;
import com.topographe.topographe.entity.enumm.Role;
import com.topographe.topographe.entity.referentiel.City;
import com.topographe.topographe.repository.ClientRepository;
import com.topographe.topographe.repository.TechnicienRepository;
import com.topographe.topographe.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TopographeMapper {

    private final ClientRepository clientRepository;
    private final TechnicienRepository technicienRepository;
    private final ProjectRepository projectRepository;

    public Topographe toEntity(TopographeCreateRequest request, City city, String encodedPassword) {
        Topographe topographe = new Topographe();
        topographe.setUsername(request.getUsername());
        topographe.setEmail(request.getEmail());
        topographe.setPassword(encodedPassword);
        topographe.setPhoneNumber(request.getPhoneNumber());
        topographe.setFirstName(request.getFirstName());
        topographe.setLastName(request.getLastName());
        topographe.setBirthday(request.getBirthday());
        topographe.setCin(request.getCin());
        topographe.setCity(city);
        topographe.setRole(Role.TOPOGRAPHE);
        topographe.setLicenseNumber(request.getLicenseNumber());
        topographe.setSpecialization(request.getSpecialization());
        topographe.setIsActive(true);
        return topographe;
    }

    public void updateEntity(Topographe topographe, TopographeUpdateRequest request, City city) {
        topographe.setEmail(request.getEmail());
        topographe.setPhoneNumber(request.getPhoneNumber());
        topographe.setFirstName(request.getFirstName());
        topographe.setLastName(request.getLastName());
        topographe.setBirthday(request.getBirthday());
        topographe.setCity(city);
        topographe.setSpecialization(request.getSpecialization());
    }

    public TopographeResponse toResponse(Topographe topographe) {
        TopographeResponse response = new TopographeResponse();
        response.setId(topographe.getId());
        response.setUsername(topographe.getUsername());
        response.setEmail(topographe.getEmail());
        response.setPhoneNumber(topographe.getPhoneNumber());
        response.setFirstName(topographe.getFirstName());
        response.setLastName(topographe.getLastName());
        response.setBirthday(topographe.getBirthday());
        response.setCin(topographe.getCin());
        response.setCityName(topographe.getCity().getName());
        response.setRole(topographe.getRole());
        response.setLicenseNumber(topographe.getLicenseNumber());
        response.setSpecialization(topographe.getSpecialization());
        response.setCreatedAt(topographe.getCreatedAt());
        response.setIsActive(topographe.getIsActive());

        // Calculer les statistiques via les repositories (évite le lazy loading)
        try {
            long totalClients = clientRepository.countByCreatedById(topographe.getId());
            long totalTechniciens = technicienRepository.countByAssignedToId(topographe.getId());
            long totalProjects = projectRepository.countByTopographeId(topographe.getId());

            response.setTotalClients((int) totalClients);
            response.setTotalTechniciens((int) totalTechniciens);
            response.setTotalProjects((int) totalProjects);

        } catch (Exception e) {
            // En cas d'erreur, mettre des valeurs par défaut
            System.err.println("Erreur lors du calcul des statistiques pour le topographe " + topographe.getId() + ": " + e.getMessage());
            response.setTotalClients(0);
            response.setTotalTechniciens(0);
            response.setTotalProjects(0);
        }

        return response;
    }
}