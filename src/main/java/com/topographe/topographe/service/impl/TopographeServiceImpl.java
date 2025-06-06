package com.topographe.topographe.service.impl;

import com.topographe.topographe.dto.request.TopographeCreateRequest;
import com.topographe.topographe.dto.request.TopographeUpdateRequest;
import com.topographe.topographe.dto.response.TopographeResponse;
import com.topographe.topographe.dto.response.UserPageResponse;
import com.topographe.topographe.entity.Topographe;
import com.topographe.topographe.entity.referentiel.City;
import com.topographe.topographe.exception.DuplicateResourceException;
import com.topographe.topographe.exception.ResourceNotFoundException;
import com.topographe.topographe.mapper.TopographeMapper;
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
    public UserPageResponse<TopographeResponse> getAllTopographes(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Topographe> topographePage = topographeRepository.findAll(pageable);

        List<TopographeResponse> topographeResponses = topographePage.getContent()
                .stream()
                .map(topographeMapper::toResponse)
                .collect(Collectors.toList());

        return new UserPageResponse<>(
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

    @Override
    public UserPageResponse<TopographeResponse> getTopographesWithFilters(
            int page, int size, String sortBy, String sortDir,
            String specialization, String cityName, Boolean isActive) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Topographe> topographePage = topographeRepository.findWithFilters(
                specialization, cityName, isActive, pageable);

        List<TopographeResponse> topographeResponses = topographePage.getContent()
                .stream()
                .map(topographeMapper::toResponse)
                .collect(Collectors.toList());

        return new UserPageResponse<>(
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

    private UserPageResponse<TopographeResponse> buildPageResponse(Page<Topographe> topographePage) {
        List<TopographeResponse> topographeResponses = topographePage.getContent()
                .stream()
                .map(topographeMapper::toResponse)
                .collect(Collectors.toList());

        return new UserPageResponse<>(
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