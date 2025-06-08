package com.topographe.topographe.repository;

import com.topographe.topographe.entity.Technicien;
import com.topographe.topographe.entity.enumm.SkillLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TechnicienRepository extends JpaRepository<Technicien, Long> {

    Optional<Technicien> findByUsername(String username);
    Optional<Technicien> findByEmail(String email);
    Optional<Technicien> findByCin(String cin);
    Optional<Technicien> findByPhoneNumber(String phoneNumber);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByCin(String cin);
    boolean existsByPhoneNumber(String phoneNumber);

    // Recherche avec filtres - Version corrigée avec CAST
    @Query("SELECT t FROM Technicien t WHERE " +
            "(:skillLevel IS NULL OR t.skillLevel = :skillLevel) AND " +
            "(:cityName IS NULL OR :cityName = '' OR LOWER(CAST(t.city.name AS string)) LIKE LOWER(CONCAT('%', :cityName, '%'))) AND " +
            "(:isActive IS NULL OR t.isActive = :isActive) AND " +
            "(:topographeId IS NULL OR t.assignedTo.id = :topographeId) AND " +
            "(:specialties IS NULL OR :specialties = '' OR LOWER(CAST(t.specialties AS string)) LIKE LOWER(CONCAT('%', :specialties, '%')))")
    Page<Technicien> findWithFilters(@Param("skillLevel") SkillLevel skillLevel,
                                     @Param("cityName") String cityName,
                                     @Param("isActive") Boolean isActive,
                                     @Param("topographeId") Long topographeId,
                                     @Param("specialties") String specialties,
                                     Pageable pageable);

    // Alternative avec requête SQL native si la requête JPQL ne fonctionne pas
    @Query(value = "SELECT * FROM users u " +
            "JOIN city c ON c.id = u.city_id " +
            "WHERE u.user_type = 'TECHNICIEN' " +
            "AND (:skillLevel IS NULL OR :skillLevel = '' OR CAST(u.skill_level AS TEXT) = :skillLevel) " +
            "AND (:cityName IS NULL OR :cityName = '' OR CAST(c.name AS TEXT) ILIKE CONCAT('%', :cityName, '%')) " +
            "AND (:isActive IS NULL OR u.is_active = :isActive) " +
            "AND (:topographeId IS NULL OR u.assigned_to_topographe_id = :topographeId) " +
            "AND (:specialties IS NULL OR :specialties = '' OR CAST(u.specialties AS TEXT) ILIKE CONCAT('%', :specialties, '%'))",
            countQuery = "SELECT COUNT(*) FROM users u " +
                    "JOIN city c ON c.id = u.city_id " +
                    "WHERE u.user_type = 'TECHNICIEN' " +
                    "AND (:skillLevel IS NULL OR :skillLevel = '' OR CAST(u.skill_level AS TEXT) = :skillLevel) " +
                    "AND (:cityName IS NULL OR :cityName = '' OR CAST(c.name AS TEXT) ILIKE CONCAT('%', :cityName, '%')) " +
                    "AND (:isActive IS NULL OR u.is_active = :isActive) " +
                    "AND (:topographeId IS NULL OR u.assigned_to_topographe_id = :topographeId) " +
                    "AND (:specialties IS NULL OR :specialties = '' OR CAST(u.specialties AS TEXT) ILIKE CONCAT('%', :specialties, '%'))",
            nativeQuery = true)
    Page<Technicien> findWithFiltersNative(@Param("skillLevel") String skillLevel,
                                           @Param("cityName") String cityName,
                                           @Param("isActive") Boolean isActive,
                                           @Param("topographeId") Long topographeId,
                                           @Param("specialties") String specialties,
                                           Pageable pageable);

    // Techniciens par topographe
    Page<Technicien> findByAssignedToId(Long topographeId, Pageable pageable);

    // Techniciens disponibles (actifs et avec peu de tâches) - CORRIGÉ
    @Query("SELECT t FROM Technicien t WHERE t.isActive = true AND " +
            "(SELECT COUNT(task) FROM Task task JOIN task.assignedTechniciens assignedTech " +
            "WHERE assignedTech = t AND task.status IN ('TODO', 'IN_PROGRESS', 'REVIEW')) < :maxTasks")
    List<Technicien> findAvailableTechniciens(@Param("maxTasks") int maxTasks);

    // Techniciens par niveau de compétence
    List<Technicien> findBySkillLevelAndIsActiveTrue(SkillLevel skillLevel);

    // Techniciens actifs
    List<Technicien> findByIsActiveTrue();

    // Statistiques
    @Query("SELECT COUNT(t) FROM Technicien t WHERE t.isActive = true")
    long countActiveTechniciens();

    @Query("SELECT COUNT(t) FROM Technicien t WHERE t.assignedTo.id = :topographeId")
    long countByTopographeId(@Param("topographeId") Long topographeId);

    @Query("SELECT t.skillLevel, COUNT(t) FROM Technicien t WHERE t.isActive = true GROUP BY t.skillLevel")
    List<Object[]> countBySkillLevel();

    // Techniciens avec le plus de tâches
    @Query("SELECT t, COUNT(task) as taskCount FROM Technicien t " +
            "LEFT JOIN Task task ON t MEMBER OF task.assignedTechniciens " +
            "WHERE t.isActive = true " +
            "GROUP BY t " +
            "ORDER BY taskCount DESC")
    List<Object[]> findTechniciensWithTaskCount(Pageable pageable);

    @Query("SELECT COUNT(t) FROM Technicien t WHERE t.assignedTo.id = :topographeId")
    long countByAssignedToId(@Param("topographeId") Long topographeId);

    // Nouvelles méthodes pour les statistiques Many-to-Many

    // Nombre de tâches par technicien par statut
    @Query("SELECT COUNT(task) FROM Task task JOIN task.assignedTechniciens tech " +
            "WHERE tech.id = :technicienId AND task.status = :status")
    long countTasksByTechnicienAndStatus(@Param("technicienId") Long technicienId,
                                         @Param("status") com.topographe.topographe.entity.enumm.TaskStatus status);

    // Nombre total de tâches par technicien
    @Query("SELECT COUNT(task) FROM Task task JOIN task.assignedTechniciens tech " +
            "WHERE tech.id = :technicienId")
    long countTasksByTechnicien(@Param("technicienId") Long technicienId);

    // Projets uniques par technicien
    @Query("SELECT COUNT(DISTINCT task.project) FROM Task task JOIN task.assignedTechniciens tech " +
            "WHERE tech.id = :technicienId")
    long countProjectsByTechnicien(@Param("technicienId") Long technicienId);

    // Projets actifs par technicien
    @Query("SELECT COUNT(DISTINCT task.project) FROM Task task JOIN task.assignedTechniciens tech " +
            "WHERE tech.id = :technicienId AND task.project.status = 'IN_PROGRESS'")
    long countActiveProjectsByTechnicien(@Param("technicienId") Long technicienId);

    // Projets terminés par technicien
    @Query("SELECT COUNT(DISTINCT task.project) FROM Task task JOIN task.assignedTechniciens tech " +
            "WHERE tech.id = :technicienId AND task.project.status = 'COMPLETED'")
    long countCompletedProjectsByTechnicien(@Param("technicienId") Long technicienId);
}