package com.topographe.topographe.mapper;

import com.topographe.topographe.dto.request.ClientCreateRequest;
import com.topographe.topographe.dto.request.ClientUpdateRequest;
import com.topographe.topographe.dto.response.ClientResponse;
import com.topographe.topographe.entity.Client;
import com.topographe.topographe.entity.Topographe;
import com.topographe.topographe.entity.enumm.Role;
import com.topographe.topographe.entity.referentiel.City;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

    public Client toEntity(ClientCreateRequest request, City city, Topographe createdBy, String encodedPassword) {
        Client client = new Client();
        client.setUsername(request.getUsername());
        client.setEmail(request.getEmail());
        client.setPassword(encodedPassword);
        client.setPhoneNumber(request.getPhoneNumber());
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setBirthday(request.getBirthday());
        client.setCin(request.getCin());
        client.setCity(city);
        client.setRole(Role.CLIENT);
        client.setClientType(request.getClientType());
        client.setCompanyName(request.getCompanyName());
        client.setCreatedBy(createdBy);
        client.setIsActive(true);
        return client;
    }

    public void updateEntity(Client client, ClientUpdateRequest request, City city) {
        client.setEmail(request.getEmail());
        client.setPhoneNumber(request.getPhoneNumber());
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setBirthday(request.getBirthday());
        client.setCity(city);
        client.setClientType(request.getClientType());
        client.setCompanyName(request.getCompanyName());
    }

    public ClientResponse toResponse(Client client) {
        ClientResponse response = new ClientResponse();
        response.setId(client.getId());
        response.setUsername(client.getUsername());
        response.setEmail(client.getEmail());
        response.setPhoneNumber(client.getPhoneNumber());
        response.setFirstName(client.getFirstName());
        response.setLastName(client.getLastName());
        response.setBirthday(client.getBirthday());
        response.setCin(client.getCin());
        response.setCityName(client.getCity().getName());
        response.setRole(client.getRole());
        response.setClientType(client.getClientType());
        response.setCompanyName(client.getCompanyName());
        response.setCreatedByTopographeName(
                client.getCreatedBy().getFirstName() + " " + client.getCreatedBy().getLastName()
        );
        response.setCreatedByTopographeId(client.getCreatedBy().getId());
        response.setCreatedAt(client.getCreatedAt());
        response.setIsActive(client.getIsActive());

        // Statistiques des projets
        if (client.getProjects() != null) {
            response.setTotalProjects(client.getProjects().size());
            response.setActiveProjects((int) client.getProjects().stream()
                    .filter(project -> project.getStatus().name().equals("IN_PROGRESS") ||
                            project.getStatus().name().equals("PLANNING"))
                    .count());
            response.setCompletedProjects((int) client.getProjects().stream()
                    .filter(project -> project.getStatus().name().equals("COMPLETED"))
                    .count());
        } else {
            response.setTotalProjects(0);
            response.setActiveProjects(0);
            response.setCompletedProjects(0);
        }

        return response;
    }
}