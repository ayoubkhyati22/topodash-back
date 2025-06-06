package com.topographe.topographe.service.impl;

import com.topographe.topographe.dto.request.TopographeCreateRequest;
import com.topographe.topographe.dto.request.TopographeUpdateRequest;
import com.topographe.topographe.dto.response.PageResponse;
import com.topographe.topographe.dto.response.TopographeResponse;
import com.topographe.topographe.entity.Topographe;
import com.topographe.topographe.entity.referentiel.City;
import com.topographe.topographe.exception.DuplicateResourceException;
import com.topographe.topographe.exception.ResourceNotFoundException;
import com.topographe.topographe.mapper.TopographeMapper;
import com.topographe.topographe.repository.ClientRepository;
import com.topographe.topographe.repository.ProjectRepository;
import com.topographe.topographe.repository.TechnicienRepository;
import com.topographe.topographe.repository.referentiel.CityRepository;
import com.topographe.topographe.repository.TopographeRepository;
import com.topographe.topographe.service.TopographeService;
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
public class TopographeServiceImpl implements TopographeService {

    private final TopographeRepository topographeRepository;
    private final ClientRepository clientRepository;
    private final TechnicienRepository technicienRepository;
    private final ProjectRepository projectRepository;
    private final CityRepository cityRepository;
    private final TopographeMapper topographeMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public TopographeResponse createTopographe(TopographeCreateRequest request) {
        // Vérifier les doublons
        validateUniqueFields(request);

        // Récupérer la ville
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("Ville non trouvée avec l'ID: " + request.getCityId()));

        // Encoder le mot de passe
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // Créer le topographe
        Topographe topographe = topographeMapper.toEntity(request, city, encodedPassword);
        Topographe savedTopographe = topographeRepository.save(topographe);

        return topographeMapper.toResponse(savedTopographe);
    }

    @Override
    public PageResponse<TopographeResponse> getAllTopographes(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Topographe> topographePage = topographeRepository.findAll(pageable);

        return buildPageResponse(topographePage);
    }

    @Override
    public PageResponse<TopographeResponse> getTopographesWithFilters(
            int page, int size, String sortBy, String sortDir,
            String specialization, String cityName, Boolean isActive) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // Normaliser les paramètres vides en null
        String normalizedSpecialization = (specialization != null && specialization.trim().isEmpty()) ? null : specialization;
        String normalizedCityName = (cityName != null && cityName.trim().isEmpty()) ? null : cityName;

        try {
            // Essayer d'abord avec la requête JPQL
            Page<Topographe> topographePage = topographeRepository.findWithFilters(
                    normalizedSpecialization, normalizedCityName, isActive, pageable);
            return buildPageResponse(topographePage);
        } catch (Exception e) {
            // Si la requête JPQL échoue, utiliser la requête native
            System.out.println("Requête JPQL échouée, utilisation de la requête native: " + e.getMessage());
            Page<Topographe> topographePage = topographeRepository.findWithFiltersNative(
                    normalizedSpecialization, normalizedCityName, isActive, pageable);
            return buildPageResponse(topographePage);
        }
    }

    @Override
    public TopographeResponse getTopographeById(Long id) {
        Topographe topographe = findTopographeById(id);
        return topographeMapper.toResponse(topographe);
    }

    @Override
    @Transactional
    public TopographeResponse updateTopographe(Long id, TopographeUpdateRequest request) {
        Topographe topographe = findTopographeById(id);

        // Vérifier les doublons (exclure l'utilisateur actuel)
        validateUniqueFieldsForUpdate(request, id);

        // Récupérer la ville
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("Ville non trouvée avec l'ID: " + request.getCityId()));

        // Mettre à jour les champs
        topographeMapper.updateEntity(topographe, request, city);
        Topographe updatedTopographe = topographeRepository.save(topographe);

        return topographeMapper.toResponse(updatedTopographe);
    }

    @Override
    @Transactional
    public void deleteTopographe(Long id) {
        Topographe topographe = findTopographeById(id);

        // Vérifier s'il a des clients ou techniciens assignés
        if (!topographe.getClients().isEmpty() || !topographe.getTechniciens().isEmpty()) {
            throw new IllegalStateException("Impossible de supprimer un topographe qui a des clients ou techniciens assignés");
        }

        topographeRepository.delete(topographe);
    }

    @Override
    @Transactional
    public void activateTopographe(Long id) {
        Topographe topographe = findTopographeById(id);
        topographe.setIsActive(true);
        topographeRepository.save(topographe);
    }

    @Override
    @Transactional
    public void deactivateTopographe(Long id) {
        Topographe topographe = findTopographeById(id);
        topographe.setIsActive(false);
        topographeRepository.save(topographe);
    }

    // Méthodes utilitaires

    private Topographe findTopographeById(Long id) {
        return topographeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topographe non trouvé avec l'ID: " + id));
    }

    private PageResponse<TopographeResponse> buildPageResponse(Page<Topographe> topographePage) {
        List<TopographeResponse> topographeResponses = topographePage.getContent()
                .stream()
                .map(topographe -> {
                    TopographeResponse response = topographeMapper.toResponse(topographe);

                    // Alternative: calculer les statistiques ici si le mapper ne fonctionne pas
                    try {
                        long totalClients = clientRepository.countByCreatedById(topographe.getId());
                        long totalTechniciens = technicienRepository.countByAssignedToId(topographe.getId());
                        long totalProjects = projectRepository.countByTopographeId(topographe.getId());

                        response.setTotalClients((int) totalClients);
                        response.setTotalTechniciens((int) totalTechniciens);
                        response.setTotalProjects((int) totalProjects);

                    } catch (Exception e) {
                        System.err.println("Erreur lors du calcul des statistiques: " + e.getMessage());
                        response.setTotalClients(0);
                        response.setTotalTechniciens(0);
                        response.setTotalProjects(0);
                    }

                    return response;
                })
                .collect(Collectors.toList());

        return new PageResponse<>(
                topographeResponses,
                topographePage.getNumber(),
                topographePage.getSize(),
                topographePage.getTotalElements(),
                topographePage.getTotalPages(),
                topographePage.isFirst(),
                topographePage.isLast(),
                topographePage.hasNext(),
                topographePage.hasPrevious()
        );
    }

    private void validateUniqueFields(TopographeCreateRequest request) {
        if (topographeRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Un utilisateur avec ce nom d'utilisateur existe déjà");
        }
        if (topographeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Un utilisateur avec cet email existe déjà");
        }
        if (topographeRepository.existsByCin(request.getCin())) {
            throw new DuplicateResourceException("Un utilisateur avec ce CIN existe déjà");
        }
        if (topographeRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("Un utilisateur avec ce numéro de téléphone existe déjà");
        }
        if (topographeRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new DuplicateResourceException("Un topographe avec ce numéro de licence existe déjà");
        }
    }

    private void validateUniqueFieldsForUpdate(TopographeUpdateRequest request, Long topographeId) {
        // Vérifier email (exclure l'utilisateur actuel)
        topographeRepository.findByEmail(request.getEmail())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(topographeId)) {
                        throw new DuplicateResourceException("Un utilisateur avec cet email existe déjà");
                    }
                });

        // Vérifier téléphone (exclure l'utilisateur actuel)
        topographeRepository.findByPhoneNumber(request.getPhoneNumber())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(topographeId)) {
                        throw new DuplicateResourceException("Un utilisateur avec ce numéro de téléphone existe déjà");
                    }
                });
    }
}