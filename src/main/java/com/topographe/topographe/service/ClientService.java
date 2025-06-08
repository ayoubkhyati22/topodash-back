package com.topographe.topographe.service;

import com.topographe.topographe.dto.request.ClientCreateRequest;
import com.topographe.topographe.dto.request.ClientUpdateRequest;
import com.topographe.topographe.dto.response.PageResponse;
import com.topographe.topographe.dto.response.ClientResponse;
import com.topographe.topographe.entity.User;
import com.topographe.topographe.entity.enumm.ClientType;

public interface ClientService {

    ClientResponse createClient(ClientCreateRequest request, User currentUser);

    PageResponse<ClientResponse> getAllClients(int page, int size, String sortBy, String sortDir, User currentUser);

    PageResponse<ClientResponse> getClientsWithFilters(
            int page, int size, String sortBy, String sortDir,
            ClientType clientType, String cityName, Boolean isActive,
            Long topographeId, String companyName, User currentUser
    );

    PageResponse<ClientResponse> getClientsByTopographe(
            Long topographeId, int page, int size, String sortBy, String sortDir, User currentUser
    );

    ClientResponse getClientById(Long id, User currentUser);

    ClientResponse updateClient(Long id, ClientUpdateRequest request, User currentUser);

    void deleteClient(Long id, User currentUser);

    void activateClient(Long id, User currentUser);

    void deactivateClient(Long id, User currentUser);

    // Méthodes statistiques
    long getTotalActiveClients(User currentUser);

    long getClientCountByTopographe(Long topographeId, User currentUser);
}