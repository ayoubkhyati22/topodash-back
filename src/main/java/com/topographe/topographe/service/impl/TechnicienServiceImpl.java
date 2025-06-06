package com.topographe.topographe.service.impl;

import com.topographe.topographe.dto.request.TechnicienCreateRequest;
import com.topographe.topographe.dto.request.TechnicienUpdateRequest;
import com.topographe.topographe.dto.response.PageResponse;
import com.topographe.topographe.dto.response.TechnicienResponse;
import com.topographe.topographe.entity.Technicien;
import com.topographe.topographe.entity.Topographe;
import com.topographe.topographe.entity.enumm.SkillLevel;
import com.topographe.topographe.entity.referentiel.City;
import com.topographe.topographe.exception.DuplicateResourceException;
import com.topographe.topographe.exception.ResourceNotFoundException;
import com.topographe.topographe.mapper.TechnicienMapper;
import com.topographe.topographe.repository.referentiel.CityRepository;
import com.topographe.topographe.repository.TechnicienRepository;
import com.topographe.topographe.repository.TopographeRepository;
import com.topographe.topographe.service.TechnicienService;
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
public class TechnicienServiceImpl implements TechnicienService {

    private final TechnicienRepository technicienRepository;
    private final TopographeRepository topographeRepository;
    private final CityRepository cityRepository;
    private final TechnicienMapper technicienMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public TechnicienResponse createTechnicien(TechnicienCreateRequest request) {
        // Vérifier les doublons
        validateUniqueFields(request);

        // Récupérer le topographe assigné
        Topographe assignedTo = topographeRepository.findById(request.getAssignedToTopographeId())
                .orElseThrow(() -> new ResourceNotFoundException("Topographe non trouvé avec l'ID: " + request.getAssignedToTopographeId()));

        // Vérifier que le topographe est actif
        if (!assignedTo.getIsActive()) {
            throw new IllegalStateException("Le topographe doit être actif pour avoir des techniciens assignés");
        }

        // Récupérer la ville
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("Ville non trouvée avec l'ID: " + request.getCityId()));

        // Encoder le mot de passe
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // Créer le technicien
        Technicien technicien = technicienMapper.toEntity(request, city, assignedTo, encodedPassword);
        Technicien savedTechnicien = technicienRepository.save(technicien);

        return technicienMapper.toResponse(savedTechnicien);
    }

    @Override
    public PageResponse<TechnicienResponse> getAllTechniciens(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Technicien> technicienPage = technicienRepository.findAll(pageable);

        return buildPageResponse(technicienPage);
    }

    @Override
    public PageResponse<TechnicienResponse> getTechniciensWithFilters(
            int page, int size, String sortBy, String sortDir,
            SkillLevel skillLevel, String cityName, Boolean isActive,
            Long topographeId, String specialties) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Technicien> technicienPage = technicienRepository.findWithFilters(
                skillLevel, cityName, isActive, topographeId, specialties, pageable);

        return buildPageResponse(technicienPage);
    }

    @Override
    public PageResponse<TechnicienResponse> getTechniciensByTopographe(
            Long topographeId, int page, int size, String sortBy, String sortDir) {

        // Vérifier que le topographe existe
        if (!topographeRepository.existsById(topographeId)) {
            throw new ResourceNotFoundException("Topographe non trouvé avec l'ID: " + topographeId);
        }

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Technicien> technicienPage = technicienRepository.findByAssignedToId(topographeId, pageable);

        return buildPageResponse(technicienPage);
    }

    @Override
    public TechnicienResponse getTechnicienById(Long id) {
        Technicien technicien = findTechnicienById(id);
        return technicienMapper.toResponse(technicien);
    }

    @Override
    @Transactional
    public TechnicienResponse updateTechnicien(Long id, TechnicienUpdateRequest request) {
        Technicien technicien = findTechnicienById(id);

        // Vérifier les doublons (exclure l'utilisateur actuel)
        validateUniqueFieldsForUpdate(request, id);

        // Récupérer le topographe assigné
        Topographe assignedTo = topographeRepository.findById(request.getAssignedToTopographeId())
                .orElseThrow(() -> new ResourceNotFoundException("Topographe non trouvé avec l'ID: " + request.getAssignedToTopographeId()));

        // Vérifier que le topographe est actif
        if (!assignedTo.getIsActive()) {
            throw new IllegalStateException("Le topographe doit être actif pour avoir des techniciens assignés");
        }

        // Récupérer la ville
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("Ville non trouvée avec l'ID: " + request.getCityId()));

        // Mettre à jour les champs
        technicienMapper.updateEntity(technicien, request, city, assignedTo);
        Technicien updatedTechnicien = technicienRepository.save(technicien);

        return technicienMapper.toResponse(updatedTechnicien);
    }

    @Override
    @Transactional
    public void deleteTechnicien(Long id) {
        Technicien technicien = findTechnicienById(id);

        // Vérifier s'il a des tâches assignées
        if (!technicien.getTasks().isEmpty()) {
            throw new IllegalStateException("Impossible de supprimer un technicien qui a des tâches assignées");
        }

        technicienRepository.delete(technicien);
    }

    @Override
    @Transactional
    public void activateTechnicien(Long id) {
        Technicien technicien = findTechnicienById(id);
        technicien.setIsActive(true);
        technicienRepository.save(technicien);
    }

    @Override
    @Transactional
    public void deactivateTechnicien(Long id) {
        Technicien technicien = findTechnicienById(id);
        technicien.setIsActive(false);
        technicienRepository.save(technicien);
    }

    @Override
    @Transactional
    public TechnicienResponse reassignTechnicien(Long technicienId, Long newTopographeId) {
        Technicien technicien = findTechnicienById(technicienId);

        Topographe newTopographe = topographeRepository.findById(newTopographeId)
                .orElseThrow(() -> new ResourceNotFoundException("Topographe non trouvé avec l'ID: " + newTopographeId));

        if (!newTopographe.getIsActive()) {
            throw new IllegalStateException("Le nouveau topographe doit être actif");
        }

        technicien.setAssignedTo(newTopographe);
        Technicien updatedTechnicien = technicienRepository.save(technicien);

        return technicienMapper.toResponse(updatedTechnicien);
    }

    @Override
    public List<TechnicienResponse> getAvailableTechniciens(int maxTasks) {
        List<Technicien> availableTechniciens = technicienRepository.findAvailableTechniciens(maxTasks);
        return availableTechniciens.stream()
                .map(technicienMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TechnicienResponse> getTechniciensBySkillLevel(SkillLevel skillLevel) {
        List<Technicien> techniciens = technicienRepository.findBySkillLevelAndIsActiveTrue(skillLevel);
        return techniciens.stream()
                .map(technicienMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long getTotalActiveTechniciens() {
        return technicienRepository.countActiveTechniciens();
    }

    @Override
    public long getTechnicienCountByTopographe(Long topographeId) {
        return technicienRepository.countByTopographeId(topographeId);
    }

    // Méthodes utilitaires

    private Technicien findTechnicienById(Long id) {
        return technicienRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technicien non trouvé avec l'ID: " + id));
    }

    private PageResponse<TechnicienResponse> buildPageResponse(Page<Technicien> technicienPage) {
        List<TechnicienResponse> technicienResponses = technicienPage.getContent()
                .stream()
                .map(technicienMapper::toResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                technicienResponses,
                technicienPage.getNumber(),
                technicienPage.getSize(),
                technicienPage.getTotalElements(),
                technicienPage.getTotalPages(),
                technicienPage.isFirst(),
                technicienPage.isLast(),
                technicienPage.hasNext(),
                technicienPage.hasPrevious()
        );
    }

    private void validateUniqueFields(TechnicienCreateRequest request) {
        if (technicienRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Un utilisateur avec ce nom d'utilisateur existe déjà");
        }
        if (technicienRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Un utilisateur avec cet email existe déjà");
        }
        if (technicienRepository.existsByCin(request.getCin())) {
            throw new DuplicateResourceException("Un utilisateur avec ce CIN existe déjà");
        }
        if (technicienRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("Un utilisateur avec ce numéro de téléphone existe déjà");
        }
    }

    private void validateUniqueFieldsForUpdate(TechnicienUpdateRequest request, Long technicienId) {
        // Vérifier email (exclure l'utilisateur actuel)
        technicienRepository.findByEmail(request.getEmail())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(technicienId)) {
                        throw new DuplicateResourceException("Un utilisateur avec cet email existe déjà");
                    }
                });

        // Vérifier téléphone (exclure l'utilisateur actuel)
        technicienRepository.findByPhoneNumber(request.getPhoneNumber())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(technicienId)) {
                        throw new DuplicateResourceException("Un utilisateur avec ce numéro de téléphone existe déjà");
                    }
                });
    }
}