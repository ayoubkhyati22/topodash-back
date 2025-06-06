package com.topographe.topographe.service;

import com.topographe.topographe.dto.request.ClientCreateRequest;
import com.topographe.topographe.dto.request.ClientUpdateRequest;
import com.topographe.topographe.dto.response.PageResponse;
import com.topographe.topographe.dto.response.ClientResponse;
import com.topographe.topographe.entity.enumm.ClientType;

public interface ClientService {

    ClientResponse createClient(ClientCreateRequest request);

    PageResponse<ClientResponse> getAllClients(int page, int size, String sortBy, String sortDir);

    PageResponse<ClientResponse> getClientsWithFilters(
            int page, int size, String sortBy, String sortDir,
            ClientType clientType, String cityName, Boolean isActive,
            Long topographeId, String companyName
    );

    PageResponse<ClientResponse> getClientsByTopographe(
            Long topographeId, int page, int size, String sortBy, String sortDir
    );

    ClientResponse getClientById(Long id);

    ClientResponse updateClient(Long id, ClientUpdateRequest request);

    void deleteClient(Long id);

    void activateClient(Long id);

    void deactivateClient(Long id);

    // Méthodes statistiques
    long getTotalActiveClients();

    long getClientCountByTopographe(Long topographeId);
}