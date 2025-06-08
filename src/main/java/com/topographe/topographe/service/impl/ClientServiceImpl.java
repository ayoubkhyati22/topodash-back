package com.topographe.topographe.service.impl;

import com.topographe.topographe.dto.request.ClientCreateRequest;
import com.topographe.topographe.dto.request.ClientUpdateRequest;
import com.topographe.topographe.dto.response.PageResponse;
import com.topographe.topographe.dto.response.ClientResponse;
import com.topographe.topographe.entity.Client;
import com.topographe.topographe.entity.Topographe;
import com.topographe.topographe.entity.User;
import com.topographe.topographe.entity.enumm.ClientType;
import com.topographe.topographe.entity.enumm.Role;
import com.topographe.topographe.entity.referentiel.City;
import com.topographe.topographe.exception.DuplicateResourceException;
import com.topographe.topographe.exception.ResourceNotFoundException;
import com.topographe.topographe.mapper.ClientMapper;
import com.topographe.topographe.repository.ClientRepository;
import com.topographe.topographe.repository.ProjectRepository;
import com.topographe.topographe.repository.referentiel.CityRepository;
import com.topographe.topographe.repository.TopographeRepository;
import com.topographe.topographe.service.ClientService;
import com.topographe.topographe.service.EmailService;
import com.topographe.topographe.util.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final TopographeRepository topographeRepository;
    private final CityRepository cityRepository;
    private final ProjectRepository projectRepository;
    private final ClientMapper clientMapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGenerator passwordGenerator;
    private final EmailService emailService;

    @Value("${app.mail.admin:admin@topographe.com}")
    private String adminEmail;

    @Override
    @Transactional
    public ClientResponse createClient(ClientCreateRequest request, User currentUser) {
        log.info("Début de la création du client: {}", request.getUsername());

        // Vérifier les doublons
        validateUniqueFields(request);

        // Déterminer le topographe créateur selon le rôle
        Topographe createdBy;
        if (currentUser.getRole() == Role.ADMIN) {
            // L'admin doit spécifier le topographe
            if (request.getCreatedByTopographeId() == null) {
                throw new IllegalArgumentException("L'administrateur doit spécifier le topographe responsable");
            }
            createdBy = topographeRepository.findById(request.getCreatedByTopographeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Topographe non trouvé avec l'ID: " + request.getCreatedByTopographeId()));
        } else if (currentUser.getRole() == Role.TOPOGRAPHE) {
            // Pour un topographe, l'affectation est automatique
            createdBy = (Topographe) currentUser;
        } else {
            throw new IllegalArgumentException("Seuls les administrateurs et topographes peuvent créer des clients");
        }

        // Vérifier que le topographe est actif
        if (!createdBy.getIsActive()) {
            throw new IllegalStateException("Le topographe doit être actif pour créer un client");
        }

        // Récupérer la ville
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("Ville non trouvée avec l'ID: " + request.getCityId()));

        // Valider le nom d'entreprise pour les types COMPANY et GOVERNMENT
        validateCompanyName(request.getClientType(), request.getCompanyName());

        // Générer un mot de passe automatiquement s'il n'est pas fourni
        String plainPassword;
        boolean passwordGenerated = false;

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            plainPassword = passwordGenerator.generateSimplePassword(10);
            passwordGenerated = true;
            log.info("Mot de passe généré automatiquement pour le client: {}", request.getUsername());
        } else {
            plainPassword = request.getPassword();
            log.info("Mot de passe fourni manuellement pour le client: {}", request.getUsername());
        }

        // Encoder le mot de passe
        String encodedPassword = passwordEncoder.encode(plainPassword);

        // Créer le client
        Client client = clientMapper.toEntity(request, city, createdBy, encodedPassword);
        Client savedClient = clientRepository.save(client);
        log.info("Client créé avec succès: {} (ID: {})", savedClient.getUsername(), savedClient.getId());

        // Préparer la réponse avec statistiques
        ClientResponse response = buildClientResponseWithStats(savedClient);

        // Envoyer l'email de bienvenue de manière asynchrone
        try {
            sendWelcomeEmailAsync(savedClient, plainPassword, createdBy);
            log.info("Email de bienvenue programmé pour le client: {}", savedClient.getEmail());
        } catch (Exception e) {
            log.error("Erreur lors de la programmation de l'email de bienvenue pour le client: {}", savedClient.getEmail(), e);
            // Ne pas faire échouer la création du client si l'email échoue
        }

        return response;
    }

    /**
     * Envoie l'email de bienvenue de manière asynchrone
     */
    private void sendWelcomeEmailAsync(Client client, String password, Topographe topographe) {
        // Utiliser un thread séparé pour éviter de bloquer la transaction
        new Thread(() -> {
            try {
                // Attendre un peu pour s'assurer que la transaction est commitée
                Thread.sleep(1000);

                // Essayer d'abord l'email HTML avec template
                try {
                    emailService.sendWelcomeEmailToClient(client, password, topographe);
                    log.info("Email HTML de bienvenue envoyé avec succès au client: {}", client.getEmail());
                } catch (Exception e) {
                    log.warn("Échec de l'email HTML, tentative avec email simple pour le client: {}", client.getEmail());
                    // Fallback vers email simple si le template échoue
                    emailService.sendSimpleWelcomeEmailToClient(client, password, topographe);
                    log.info("Email simple de bienvenue envoyé avec succès au client: {}", client.getEmail());
                }

                // Envoyer notification au topographe responsable
                try {
                    emailService.sendClientCreationNotificationToTopographe(client, topographe);
                } catch (Exception e) {
                    log.warn("Échec de l'envoi de la notification au topographe pour le client: {}", client.getEmail(), e);
                }

            } catch (Exception e) {
                log.error("Erreur complète lors de l'envoi des emails pour le client: {}", client.getEmail(), e);
            }
        }).start();
    }

    @Override
    public PageResponse<ClientResponse> getAllClients(int page, int size, String sortBy, String sortDir, User currentUser) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Client> clientPage;

        if (currentUser.getRole() == Role.ADMIN) {
            // L'admin voit tous les clients
            clientPage = clientRepository.findAll(pageable);
        } else if (currentUser.getRole() == Role.TOPOGRAPHE) {
            // Le topographe ne voit que ses clients
            clientPage = clientRepository.findByCreatedById(currentUser.getId(), pageable);
        } else {
            throw new IllegalArgumentException("Accès non autorisé");
        }

        return buildPageResponseWithStats(clientPage);
    }

    // Corrections à apporter dans ClientServiceImpl.java pour la méthode getClientsWithFilters

    @Override
    public PageResponse<ClientResponse> getClientsWithFilters(
            int page, int size, String sortBy, String sortDir,
            ClientType clientType, String cityName, Boolean isActive,
            Long topographeId, String companyName, User currentUser) {

        log.info("Searching clients with filters - clientType: {}, cityName: {}, isActive: {}, topographeId: {}, companyName: {}",
                clientType, cityName, isActive, topographeId, companyName);

        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() :
                    Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Client> clientPage;

            if (currentUser.getRole() == Role.ADMIN) {
                // L'admin peut chercher avec tous les filtres
                log.info("Admin search with all filters");
                clientPage = clientRepository.findWithFilters(
                        clientType, cityName, isActive, topographeId, companyName, pageable);
            } else if (currentUser.getRole() == Role.TOPOGRAPHE) {
                // Le topographe ne peut chercher que parmi ses clients
                log.info("Topographe search limited to own clients, topographeId forced to: {}", currentUser.getId());
                clientPage = clientRepository.findWithFilters(
                        clientType, cityName, isActive, currentUser.getId(), companyName, pageable);
            } else {
                throw new IllegalArgumentException("Accès non autorisé pour le rôle: " + currentUser.getRole());
            }

            log.info("Search returned {} clients out of {} total",
                    clientPage.getNumberOfElements(), clientPage.getTotalElements());

            return buildPageResponseWithStats(clientPage);

        } catch (Exception e) {
            log.error("Error during client search with filters", e);

            // En cas d'erreur, retourner une page vide plutôt que de faire échouer la requête
            return new PageResponse<>(
                    Collections.emptyList(),
                    page,
                    size,
                    0L,
                    0,
                    true,
                    true,
                    false,
                    false
            );
        }
    }

    @Override
    public PageResponse<ClientResponse> getClientsByTopographe(
            Long topographeId, int page, int size, String sortBy, String sortDir, User currentUser) {

        // Vérifications de sécurité
        if (currentUser.getRole() == Role.TOPOGRAPHE && !currentUser.getId().equals(topographeId)) {
            throw new IllegalArgumentException("Un topographe ne peut voir que ses propres clients");
        }

        // Vérifier que le topographe existe
        if (!topographeRepository.existsById(topographeId)) {
            throw new ResourceNotFoundException("Topographe non trouvé avec l'ID: " + topographeId);
        }

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Client> clientPage = clientRepository.findByCreatedById(topographeId, pageable);

        return buildPageResponseWithStats(clientPage);
    }

    @Override
    public ClientResponse getClientById(Long id, User currentUser) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + id));

        // Vérifications de sécurité
        if (currentUser.getRole() == Role.TOPOGRAPHE &&
                !client.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Un topographe ne peut voir que ses propres clients");
        }

        return buildClientResponseWithStats(client);
    }

    @Override
    @Transactional
    public ClientResponse updateClient(Long id, ClientUpdateRequest request, User currentUser) {
        Client client = findClientById(id);

        // Vérifications de sécurité
        if (currentUser.getRole() == Role.TOPOGRAPHE &&
                !client.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Un topographe ne peut modifier que ses propres clients");
        }

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

        return buildClientResponseWithStats(updatedClient);
    }

    @Override
    @Transactional
    public void deleteClient(Long id, User currentUser) {
        Client client = findClientById(id);

        // Vérifications de sécurité
        if (currentUser.getRole() == Role.TOPOGRAPHE &&
                !client.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Un topographe ne peut supprimer que ses propres clients");
        }

        // Vérifier s'il a des projets
        long projectCount = projectRepository.countByClientId(id);
        if (projectCount > 0) {
            throw new IllegalStateException("Impossible de supprimer un client qui a des projets");
        }

        clientRepository.delete(client);
    }

    @Override
    @Transactional
    public void activateClient(Long id, User currentUser) {
        Client client = findClientById(id);

        // Vérifications de sécurité
        if (currentUser.getRole() == Role.TOPOGRAPHE &&
                !client.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Un topographe ne peut activer que ses propres clients");
        }

        client.setIsActive(true);
        clientRepository.save(client);
    }

    @Override
    @Transactional
    public void deactivateClient(Long id, User currentUser) {
        Client client = findClientById(id);

        // Vérifications de sécurité
        if (currentUser.getRole() == Role.TOPOGRAPHE &&
                !client.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Un topographe ne peut désactiver que ses propres clients");
        }

        client.setIsActive(false);
        clientRepository.save(client);
    }

    @Override
    public long getTotalActiveClients(User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return clientRepository.countActiveClients();
        } else if (currentUser.getRole() == Role.TOPOGRAPHE) {
            return clientRepository.countByCreatedById(currentUser.getId());
        } else {
            throw new IllegalArgumentException("Accès non autorisé");
        }
    }

    @Override
    public long getClientCountByTopographe(Long topographeId, User currentUser) {
        // Vérifications de sécurité
        if (currentUser.getRole() == Role.TOPOGRAPHE && !currentUser.getId().equals(topographeId)) {
            throw new IllegalArgumentException("Un topographe ne peut voir que ses propres statistiques");
        }

        return clientRepository.countByCreatedById(topographeId);
    }

    // Méthodes utilitaires

    private Client findClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + id));
    }

    private ClientResponse buildClientResponseWithStats(Client client) {
        ClientResponse response = clientMapper.toResponse(client);

        try {
            long totalProjects = projectRepository.countTotalProjectsByClientId(client.getId());
            long activeProjects = projectRepository.countActiveProjectsByClientId(client.getId());
            long completedProjects = projectRepository.countCompletedProjectsByClientId(client.getId());

            response.setTotalProjects((int) totalProjects);
            response.setActiveProjects((int) activeProjects);
            response.setCompletedProjects((int) completedProjects);

            log.debug("Statistiques calculées pour client {} ({}): total={}, actifs={}, terminés={}",
                    client.getId(), client.getUsername(), totalProjects, activeProjects, completedProjects);

        } catch (Exception e) {
            log.error("Erreur lors du calcul des statistiques pour le client {}: {}", client.getId(), e.getMessage());
        }

        return response;
    }

    private PageResponse<ClientResponse> buildPageResponseWithStats(Page<Client> clientPage) {
        List<ClientResponse> clientResponses = clientPage.getContent()
                .stream()
                .map(this::buildClientResponseWithStats)
                .collect(Collectors.toList());

        return new PageResponse<>(
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
        clientRepository.findByEmail(request.getEmail())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(clientId)) {
                        throw new DuplicateResourceException("Un utilisateur avec cet email existe déjà");
                    }
                });

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