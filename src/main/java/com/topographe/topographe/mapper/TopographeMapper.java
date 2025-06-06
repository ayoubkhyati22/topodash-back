package com.topographe.topographe.mapper;

import com.topographe.topographe.dto.request.TopographeCreateRequest;
import com.topographe.topographe.dto.request.TopographeUpdateRequest;
import com.topographe.topographe.dto.response.TopographeResponse;
import com.topographe.topographe.entity.Topographe;
import com.topographe.topographe.entity.enumm.Role;
import com.topographe.topographe.entity.referentiel.City;
import org.springframework.stereotype.Component;

@Component
public class TopographeMapper {

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

        // Statistiques
        response.setTotalClients(topographe.getClients() != null ? topographe.getClients().size() : 0);
        response.setTotalTechniciens(topographe.getTechniciens() != null ? topographe.getTechniciens().size() : 0);
        response.setTotalProjects(topographe.getProjects() != null ? topographe.getProjects().size() : 0);

        return response;
    }
}