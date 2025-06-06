package com.topographe.topographe.service.impl;

import com.topographe.topographe.dto.request.ClientCreateRequest;
import com.topographe.topographe.dto.request.ClientUpdateRequest;
import com.topographe.topographe.dto.response.UserPageResponse;
import com.topographe.topographe.dto.response.ClientResponse;
import com.topographe.topographe.entity.Client;
import com.topographe.topographe.entity.Topographe;
import com.topographe.topographe.entity.enumm.ClientType;
import com.topographe.topographe.entity.referentiel.City;
import com.topographe.topographe.exception.DuplicateResourceException;
import com.topographe.topographe.exception.ResourceNotFoundException;
import com.topographe.topographe.mapper.ClientMapper;
import com.topographe.topographe.repository.ClientRepository;
import com.topographe.topographe.repository.referentiel.CityRepository;
import com.topographe.topographe.repository.TopographeRepository;
import com.topographe.topographe.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final TopographeRepository topographeRepository;
    private final CityRepository cityRepository;
    private final ClientMapper clientMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ClientResponse createClient(ClientCreateRequest request) {
        // Vérifier les doublons
        validateUniqueFields(request);

        // Récupérer le topographe créateur
        Topographe createdBy = topographeRepository.findById(request.getCreatedByTopographeId())
                .orElseThrow(() -> new ResourceNotFoundException("Topographe non trouvé avec l'ID: " + request.getCreatedByTopographeId()));

        // Vérifier que le topographe est actif
        if (!createdBy.getIsActive()) {
            throw new IllegalStateException("Le topographe doit être actif pour créer un client");
        }

        // Récupérer la ville
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("Ville non trouvée avec l'ID: " + request.getCityId()));

        // Valider le nom d'entreprise pour les types COMPANY et GOVERNMENT
        validateCompanyName(request.getClientType(), request.getCompanyName());

        // Encoder le mot de passe
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // Créer le client
        Client client = clientMapper.toEntity(request, city, createdBy, encodedPassword);
        Client savedClient = clientRepository.save(client);

        return clientMapper.toResponse(savedClient);
    }

    @Override
    public UserPageResponse<ClientResponse> getAllClients(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Client> clientPage = clientRepository.findAll(pageable);

        return buildPageResponse(clientPage);
    }

    @Override
    public UserPageResponse<ClientResponse> getClientsWithFilters(
            int page, int size, String sortBy, String sortDir,
            ClientType clientType, String cityName, Boolean isActive,
            Long topographeId, String companyName) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Client> clientPage = clientRepository.findWithFilters(
                clientType, cityName, isActive, topographeId, companyName, pageable);

        return buildPageResponse(clientPage);
    }

    @Override
    public UserPageResponse<ClientResponse> getClientsByTopographe(
            Long topographeId, int page, int size, String sortBy, String sortDir) {

        // Vérifier que le topographe existe
        if (!topographeRepository.existsById(topographeId)) {
            throw new ResourceNotFoundException("Topographe non trouvé avec l'ID: " + topographeId);
        }

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Client> clientPage = clientRepository.findByCreatedById(topographeId, pageable);

        return buildPageResponse(clientPage);
    }

    @Override
    public ClientResponse getClientById(Long id) {
        Client client = findClientById(id);
        return clientMapper.toResponse(client);
    }

    @Override
    @Transactional
    public ClientResponse updateClient(Long id, ClientUpdateRequest request) {
        Client client = findClientById(id);

        // Vérifier les doublons (exclure l'utilisateur actuel)
        validateUniqueFieldsForUpdate(request, id);

        // Récupérer la ville
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("Ville non trouvée avec l'ID: " + request.getCityId()));

        // Valider le nom d'entreprise
        validateCompanyName(request.getClientType(), request.getCompanyName());

        // Mettre à jour les champs
        clientMapper.updateEntity(client, request, city);
        Client updatedClient = clientRepository.save(client);

        return clientMapper.toResponse(updatedClient);
    }

    @Override
    @Transactional
    public void deleteClient(Long id) {
        Client client = findClientById(id);

        // Vérifier s'il a des projets
        if (!client.getProjects().isEmpty()) {
            throw new IllegalStateException("Impossible de supprimer un client qui a des projets");
        }

        clientRepository.delete(client);
    }

    @Override
    @Transactional
    public void activateClient(Long id) {
        Client client = findClientById(id);
        client.setIsActive(true);
        clientRepository.save(client);
    }

    @Override
    @Transactional
    public void deactivateClient(Long id) {
        Client client = findClientById(id);
        client.setIsActive(false);
        clientRepository.save(client);
    }

    @Override
    public long getTotalActiveClients() {
        return clientRepository.countActiveClients();
    }

    @Override
    public long getClientCountByTopographe(Long topographeId) {
        return clientRepository.countByTopographeId(topographeId);
    }

    // Méthodes utilitaires

    private Client findClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + id));
    }

    private UserPageResponse<ClientResponse> buildPageResponse(Page<Client> clientPage) {
        List<ClientResponse> clientResponses = clientPage.getContent()
                .stream()
                .map(clientMapper::toResponse)
                .collect(Collectors.toList());

        return new UserPageResponse<>(
                clientResponses,
                clientPage.getNumber(),
                clientPage.getSize(),
                clientPage.getTotalElements(),
                clientPage.getTotalPages(),
                clientPage.isFirst(),
                clientPage.isLast(),
                clientPage.hasNext(),
                clientPage.hasPrevious()
        );
    }

    private void validateUniqueFields(ClientCreateRequest request) {
        if (clientRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Un utilisateur avec ce nom d'utilisateur existe déjà");
        }
        if (clientRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Un utilisateur avec cet email existe déjà");
        }
        if (clientRepository.existsByCin(request.getCin())) {
            throw new DuplicateResourceException("Un utilisateur avec ce CIN existe déjà");
        }
        if (clientRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("Un utilisateur avec ce numéro de téléphone existe déjà");
        }
    }

    private void validateUniqueFieldsForUpdate(ClientUpdateRequest request, Long clientId) {
        // Vérifier email (exclure l'utilisateur actuel)
        clientRepository.findByEmail(request.getEmail())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(clientId)) {
                        throw new DuplicateResourceException("Un utilisateur avec cet email existe déjà");
                    }
                });

        // Vérifier téléphone (exclure l'utilisateur actuel)
        clientRepository.findByPhoneNumber(request.getPhoneNumber())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(clientId)) {
                        throw new DuplicateResourceException("Un utilisateur avec ce numéro de téléphone existe déjà");
                    }
                });
    }

    private void validateCompanyName(ClientType clientType, String companyName) {
        if ((clientType == ClientType.COMPANY || clientType == ClientType.GOVERNMENT)) {
            if (companyName == null || companyName.trim().isEmpty()) {
                throw new IllegalArgumentException("Le nom de l'entreprise est obligatoire pour les clients de type COMPANY ou GOVERNMENT");
            }
        }
    }
}