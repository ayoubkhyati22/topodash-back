package com.topographe.topographe.service.impl;

import com.topographe.topographe.dto.request.TechnicienCreateRequest;
import com.topographe.topographe.dto.request.TechnicienUpdateRequest;
import com.topographe.topographe.dto.response.PageResponse;
import com.topographe.topographe.dto.response.TechnicienResponse;
import com.topographe.topographe.entity.Technicien;
import com.topographe.topographe.entity.Topographe;
import com.topographe.topographe.entity.User;
import com.topographe.topographe.entity.enumm.Role;
import com.topographe.topographe.entity.enumm.SkillLevel;
import com.topographe.topographe.entity.referentiel.City;
import com.topographe.topographe.exception.DuplicateResourceException;
import com.topographe.topographe.exception.ResourceNotFoundException;
import com.topographe.topographe.mapper.TechnicienMapper;
import com.topographe.topographe.repository.referentiel.CityRepository;
import com.topographe.topographe.repository.TechnicienRepository;
import com.topographe.topographe.repository.TopographeRepository;
import com.topographe.topographe.service.EmailService;
import com.topographe.topographe.service.TechnicienService;
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
public class TechnicienServiceImpl implements TechnicienService {

    private final TechnicienRepository technicienRepository;
    private final TopographeRepository topographeRepository;
    private final CityRepository cityRepository;
    private final TechnicienMapper technicienMapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGenerator passwordGenerator;
    private final EmailService emailService;

    @Value("${app.mail.admin:admin@topographe.com}")
    private String adminEmail;

    @Override
    @Transactional
    public TechnicienResponse createTechnicien(TechnicienCreateRequest request, User currentUser) {
        log.info("Début de la création du technicien: {}", request.getUsername());

        // Vérifier les doublons
        validateUniqueFields(request);

        // Déterminer le topographe responsable selon le rôle
        Topographe assignedTo;
        if (currentUser.getRole() == Role.ADMIN) {
            // L'admin doit spécifier le topographe
            if (request.getAssignedToTopographeId() == null) {
                throw new IllegalArgumentException("L'administrateur doit spécifier le topographe responsable");
            }
            assignedTo = topographeRepository.findById(request.getAssignedToTopographeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Topographe non trouvé avec l'ID: " + request.getAssignedToTopographeId()));
        } else if (currentUser.getRole() == Role.TOPOGRAPHE) {
            // Pour un topographe, l'affectation est automatique à lui-même
            assignedTo = (Topographe) currentUser;
        } else {
            throw new IllegalArgumentException("Seuls les administrateurs et topographes peuvent créer des techniciens");
        }

        // Vérifier que le topographe est actif
        if (!assignedTo.getIsActive()) {
            throw new IllegalStateException("Le topographe doit être actif pour avoir des techniciens assignés");
        }

        // Récupérer la ville
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("Ville non trouvée avec l'ID: " + request.getCityId()));

        // Générer un mot de passe automatiquement s'il n'est pas fourni
        String plainPassword;
        boolean passwordGenerated = false;

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            plainPassword = passwordGenerator.generateSimplePassword(10);
            passwordGenerated = true;
            log.info("Mot de passe généré automatiquement pour le technicien: {}", request.getUsername());
        } else {
            plainPassword = request.getPassword();
            log.info("Mot de passe fourni manuellement pour le technicien: {}", request.getUsername());
        }

        // Encoder le mot de passe
        String encodedPassword = passwordEncoder.encode(plainPassword);

        // Créer le technicien
        Technicien technicien = technicienMapper.toEntity(request, city, assignedTo, encodedPassword);
        Technicien savedTechnicien = technicienRepository.save(technicien);
        log.info("Technicien créé avec succès: {} (ID: {})", savedTechnicien.getUsername(), savedTechnicien.getId());

        // Préparer la réponse
        TechnicienResponse response = technicienMapper.toResponse(savedTechnicien);

        // Envoyer l'email de bienvenue de manière asynchrone
        try {
            sendWelcomeEmailAsync(savedTechnicien, plainPassword, assignedTo);
            log.info("Email de bienvenue programmé pour le technicien: {}", savedTechnicien.getEmail());
        } catch (Exception e) {
            log.error("Erreur lors de la programmation de l'email de bienvenue pour le technicien: {}", savedTechnicien.getEmail(), e);
            // Ne pas faire échouer la création du technicien si l'email échoue
        }

        return response;
    }

    /**
     * Envoie l'email de bienvenue de manière asynchrone
     */
    private void sendWelcomeEmailAsync(Technicien technicien, String password, Topographe topographe) {
        // Utiliser un thread séparé pour éviter de bloquer la transaction
        new Thread(() -> {
            try {
                // Attendre un peu pour s'assurer que la transaction est commitée
                Thread.sleep(1000);

                // Envoyer email simple de bienvenue (adapter selon vos besoins)
                sendSimpleWelcomeEmailToTechnicien(technicien, password, topographe);
                log.info("Email simple de bienvenue envoyé avec succès au technicien: {}", technicien.getEmail());

                // Envoyer notification au topographe responsable
                try {
                    sendTechnicienCreationNotificationToTopographe(technicien, topographe);
                } catch (Exception e) {
                    log.warn("Échec de l'envoi de la notification au topographe pour le technicien: {}", technicien.getEmail(), e);
                }

            } catch (Exception e) {
                log.error("Erreur complète lors de l'envoi des emails pour le technicien: {}", technicien.getEmail(), e);
            }
        }).start();
    }

    private void sendSimpleWelcomeEmailToTechnicien(Technicien technicien, String password, Topographe topographe) {
        // À implémenter dans EmailService si nécessaire
        log.info("Email de bienvenue pour technicien: {}", technicien.getEmail());
    }

    private void sendTechnicienCreationNotificationToTopographe(Technicien technicien, Topographe topographe) {
        // À implémenter dans EmailService si nécessaire
        log.info("Notification de création technicien pour topographe: {}", topographe.getEmail());
    }

    @Override
    public PageResponse<TechnicienResponse> getAllTechniciens(int page, int size, String sortBy, String sortDir, User currentUser) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Technicien> technicienPage;

        if (currentUser.getRole() == Role.ADMIN) {
            // L'admin voit tous les techniciens
            technicienPage = technicienRepository.findAll(pageable);
        } else if (currentUser.getRole() == Role.TOPOGRAPHE) {
            // Le topographe ne voit que ses techniciens
            technicienPage = technicienRepository.findByAssignedToId(currentUser.getId(), pageable);
        } else {
            throw new IllegalArgumentException("Accès non autorisé");
        }

        return buildPageResponse(technicienPage);
    }

    @Override
    public PageResponse<TechnicienResponse> getTechniciensWithFilters(
            int page, int size, String sortBy, String sortDir,
            SkillLevel skillLevel, String cityName, Boolean isActive,
            Long topographeId, String specialties, User currentUser) {

        log.info("Searching techniciens with filters - skillLevel: {}, cityName: {}, isActive: {}, topographeId: {}, specialties: {}",
                skillLevel, cityName, isActive, topographeId, specialties);

        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() :
                    Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Technicien> technicienPage;

            // Normaliser les paramètres vides en null
            String normalizedCityName = (cityName != null && cityName.trim().isEmpty()) ? null : cityName;
            String normalizedSpecialties = (specialties != null && specialties.trim().isEmpty()) ? null : specialties;

            if (currentUser.getRole() == Role.ADMIN) {
                // L'admin peut chercher avec tous les filtres
                log.info("Admin search with all filters");

                try {
                    // Essayer d'abord avec la requête JPQL
                    technicienPage = technicienRepository.findWithFilters(
                            skillLevel, normalizedCityName, isActive, topographeId, normalizedSpecialties, pageable);
                } catch (Exception e) {
                    // Si la requête JPQL échoue, utiliser la requête native
                    log.warn("Requête JPQL échouée, utilisation de la requête native: {}", e.getMessage());
                    String skillLevelStr = skillLevel != null ? skillLevel.name() : null;
                    technicienPage = technicienRepository.findWithFiltersNative(
                            skillLevelStr, normalizedCityName, isActive, topographeId, normalizedSpecialties, pageable);
                }
            } else if (currentUser.getRole() == Role.TOPOGRAPHE) {
                // Le topographe ne peut chercher que parmi ses techniciens
                log.info("Topographe search limited to own techniciens, topographeId forced to: {}", currentUser.getId());

                try {
                    // Essayer d'abord avec la requête JPQL
                    technicienPage = technicienRepository.findWithFilters(
                            skillLevel, normalizedCityName, isActive, currentUser.getId(), normalizedSpecialties, pageable);
                } catch (Exception e) {
                    // Si la requête JPQL échoue, utiliser la requête native
                    log.warn("Requête JPQL échouée, utilisation de la requête native: {}", e.getMessage());
                    String skillLevelStr = skillLevel != null ? skillLevel.name() : null;
                    technicienPage = technicienRepository.findWithFiltersNative(
                            skillLevelStr, normalizedCityName, isActive, currentUser.getId(), normalizedSpecialties, pageable);
                }
            } else {
                throw new IllegalArgumentException("Accès non autorisé pour le rôle: " + currentUser.getRole());
            }

            log.info("Search returned {} techniciens out of {} total",
                    technicienPage.getNumberOfElements(), technicienPage.getTotalElements());

            return buildPageResponse(technicienPage);

        } catch (Exception e) {
            log.error("Error during technicien search with filters", e);

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
    public PageResponse<TechnicienResponse> getTechniciensByTopographe(
            Long topographeId, int page, int size, String sortBy, String sortDir, User currentUser) {

        // Vérifications de sécurité
        if (currentUser.getRole() == Role.TOPOGRAPHE && !currentUser.getId().equals(topographeId)) {
            throw new IllegalArgumentException("Un topographe ne peut voir que ses propres techniciens");
        }

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
    public TechnicienResponse getTechnicienById(Long id, User currentUser) {
        Technicien technicien = findTechnicienById(id);

        // Vérifications de sécurité
        if (currentUser.getRole() == Role.TOPOGRAPHE &&
                !technicien.getAssignedTo().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Un topographe ne peut voir que ses propres techniciens");
        }

        // CORRECTION: Utiliser la méthode qui calcule les statistiques
        return buildTechnicienResponseWithStats(technicien);
    }

    @Override
    @Transactional
    public TechnicienResponse updateTechnicien(Long id, TechnicienUpdateRequest request, User currentUser) {
        Technicien technicien = findTechnicienById(id);

        // Vérifications de sécurité
        if (currentUser.getRole() == Role.TOPOGRAPHE &&
                !technicien.getAssignedTo().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Un topographe ne peut modifier que ses propres techniciens");
        }

        // Vérifier les doublons (exclure l'utilisateur actuel)
        validateUniqueFieldsForUpdate(request, id);

        // Récupérer le topographe assigné
        Topographe assignedTo;
        if (currentUser.getRole() == Role.ADMIN) {
            assignedTo = topographeRepository.findById(request.getAssignedToTopographeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Topographe non trouvé avec l'ID: " + request.getAssignedToTopographeId()));
        } else {
            // Pour un topographe, il ne peut réassigner qu'à lui-même
            assignedTo = (Topographe) currentUser;
        }

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
    public void deleteTechnicien(Long id, User currentUser) {
        Technicien technicien = findTechnicienById(id);

        // Vérifications de sécurité
        if (currentUser.getRole() == Role.TOPOGRAPHE &&
                !technicien.getAssignedTo().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Un topographe ne peut supprimer que ses propres techniciens");
        }

        // Vérifier s'il a des tâches assignées
        if (!technicien.getTasks().isEmpty()) {
            throw new IllegalStateException("Impossible de supprimer un technicien qui a des tâches assignées");
        }

        technicienRepository.delete(technicien);
    }

    @Override
    @Transactional
    public void activateTechnicien(Long id, User currentUser) {
        Technicien technicien = findTechnicienById(id);

        // Vérifications de sécurité
        if (currentUser.getRole() == Role.TOPOGRAPHE &&
                !technicien.getAssignedTo().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Un topographe ne peut activer que ses propres techniciens");
        }

        technicien.setIsActive(true);
        technicienRepository.save(technicien);
    }

    @Override
    @Transactional
    public void deactivateTechnicien(Long id, User currentUser) {
        Technicien technicien = findTechnicienById(id);

        // Vérifications de sécurité
        if (currentUser.getRole() == Role.TOPOGRAPHE &&
                !technicien.getAssignedTo().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Un topographe ne peut désactiver que ses propres techniciens");
        }

        technicien.setIsActive(false);
        technicienRepository.save(technicien);
    }

    @Override
    @Transactional
    public TechnicienResponse reassignTechnicien(Long technicienId, Long newTopographeId, User currentUser) {
        // Seuls les admins peuvent réassigner
        if (currentUser.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Seuls les administrateurs peuvent réassigner des techniciens");
        }

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
    public List<TechnicienResponse> getAvailableTechniciens(int maxTasks, User currentUser) {
        List<Technicien> availableTechniciens;

        if (currentUser.getRole() == Role.ADMIN) {
            availableTechniciens = technicienRepository.findAvailableTechniciens(maxTasks);
        } else if (currentUser.getRole() == Role.TOPOGRAPHE) {
            // Le topographe ne voit que ses techniciens disponibles
            availableTechniciens = technicienRepository.findAvailableTechniciens(maxTasks)
                    .stream()
                    .filter(t -> t.getAssignedTo().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("Accès non autorisé");
        }

        return availableTechniciens.stream()
                .map(technicienMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TechnicienResponse> getTechniciensBySkillLevel(SkillLevel skillLevel, User currentUser) {
        List<Technicien> techniciens;

        if (currentUser.getRole() == Role.ADMIN) {
            techniciens = technicienRepository.findBySkillLevelAndIsActiveTrue(skillLevel);
        } else if (currentUser.getRole() == Role.TOPOGRAPHE) {
            // Le topographe ne voit que ses techniciens
            techniciens = technicienRepository.findBySkillLevelAndIsActiveTrue(skillLevel)
                    .stream()
                    .filter(t -> t.getAssignedTo().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("Accès non autorisé");
        }

        return techniciens.stream()
                .map(technicienMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long getTotalActiveTechniciens(User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return technicienRepository.countActiveTechniciens();
        } else if (currentUser.getRole() == Role.TOPOGRAPHE) {
            return technicienRepository.countByAssignedToId(currentUser.getId());
        } else {
            throw new IllegalArgumentException("Accès non autorisé");
        }
    }

    @Override
    public long getTechnicienCountByTopographe(Long topographeId, User currentUser) {
        // Vérifications de sécurité
        if (currentUser.getRole() == Role.TOPOGRAPHE && !currentUser.getId().equals(topographeId)) {
            throw new IllegalArgumentException("Un topographe ne peut voir que ses propres statistiques");
        }

        return technicienRepository.countByAssignedToId(topographeId);
    }

    // Méthodes utilitaires

    private Technicien findTechnicienById(Long id) {
        return technicienRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technicien non trouvé avec l'ID: " + id));
    }

    private PageResponse<TechnicienResponse> buildPageResponse(Page<Technicien> technicienPage) {
        List<TechnicienResponse> technicienResponses = technicienPage.getContent()
                .stream()
                .map(this::buildTechnicienResponseWithStats) // ← CORRECTION ICI
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

    private TechnicienResponse buildTechnicienResponseWithStats(Technicien technicien) {
        TechnicienResponse response = technicienMapper.toResponse(technicien);

        try {
            log.debug("Calcul des statistiques pour le technicien ID: {}", technicien.getId());

            // Calculer les statistiques des tâches via le repository
            long totalTasks = technicienRepository.countTasksByTechnicien(technicien.getId());
            long activeTasks = technicienRepository.countTasksByTechnicienAndStatus(
                    technicien.getId(), com.topographe.topographe.entity.enumm.TaskStatus.IN_PROGRESS);
            long completedTasks = technicienRepository.countTasksByTechnicienAndStatus(
                    technicien.getId(), com.topographe.topographe.entity.enumm.TaskStatus.COMPLETED);
            long todoTasks = technicienRepository.countTasksByTechnicienAndStatus(
                    technicien.getId(), com.topographe.topographe.entity.enumm.TaskStatus.TODO);
            long reviewTasks = technicienRepository.countTasksByTechnicienAndStatus(
                    technicien.getId(), com.topographe.topographe.entity.enumm.TaskStatus.REVIEW);

            // Calculer les statistiques des projets
            long totalProjects = technicienRepository.countProjectsByTechnicien(technicien.getId());
            long activeProjects = technicienRepository.countActiveProjectsByTechnicien(technicien.getId());
            long completedProjects = technicienRepository.countCompletedProjectsByTechnicien(technicien.getId());

            // Mettre à jour la réponse avec les valeurs calculées
            response.setTotalTasks((int) totalTasks);
            response.setActiveTasks((int) activeTasks);
            response.setCompletedTasks((int) completedTasks);
            response.setTodoTasks((int) todoTasks);
            response.setReviewTasks((int) reviewTasks);
            response.setTotalProjects((int) totalProjects);
            response.setActiveProjects((int) activeProjects);
            response.setCompletedProjects((int) completedProjects);

            // Calcul des indicateurs de performance
            double workloadPercentage = Math.min((activeTasks / 5.0) * 100, 100); // Max 5 tâches = 100%
            response.setWorkloadPercentage(Math.round(workloadPercentage * 100.0) / 100.0);
            response.setAvailable(activeTasks < 5);
            response.setMaxRecommendedTasks(5);

            // Calcul du taux de completion
            if (totalTasks > 0) {
                double completionRate = ((double) completedTasks / totalTasks) * 100;
                response.setCompletionRate(Math.round(completionRate * 100.0) / 100.0);
            } else {
                response.setCompletionRate(0.0);
            }

            // Calcul de la moyenne de tâches par projet
            if (totalProjects > 0) {
                double avgTasksPerProject = (double) totalTasks / totalProjects;
                response.setAverageTasksPerProject(Math.round(avgTasksPerProject * 100.0) / 100.0);
            } else {
                response.setAverageTasksPerProject(0.0);
            }

            log.debug("Statistiques calculées pour technicien {} ({}): total={}, actifs={}, projets={}",
                    technicien.getId(), technicien.getUsername(), totalTasks, activeTasks, totalProjects);

        } catch (Exception e) {
            log.error("Erreur lors du calcul des statistiques pour le technicien {}: {}",
                    technicien.getId(), e.getMessage(), e);

            // En cas d'erreur, garder les valeurs par défaut
            response.setTotalTasks(0);
            response.setActiveTasks(0);
            response.setCompletedTasks(0);
            response.setTodoTasks(0);
            response.setReviewTasks(0);
            response.setTotalProjects(0);
            response.setActiveProjects(0);
            response.setCompletedProjects(0);
            response.setWorkloadPercentage(0.0);
            response.setAvailable(true);
            response.setMaxRecommendedTasks(5);
            response.setCompletionRate(0.0);
            response.setAverageTasksPerProject(0.0);
        }

        return response;
    }

    private PageResponse<TechnicienResponse> buildPageResponseWithStats(Page<Technicien> technicienPage) {
        List<TechnicienResponse> technicienResponses = technicienPage.getContent()
                .stream()
                .map(this::buildTechnicienResponseWithStats)
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
}