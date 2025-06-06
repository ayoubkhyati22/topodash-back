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

    // Recherche avec filtres
    @Query("SELECT t FROM Technicien t WHERE " +
            "(:skillLevel IS NULL OR t.skillLevel = :skillLevel) AND " +
            "(:cityName IS NULL OR LOWER(t.city.name) LIKE LOWER(CONCAT('%', :cityName, '%'))) AND " +
            "(:isActive IS NULL OR t.isActive = :isActive) AND " +
            "(:topographeId IS NULL OR t.assignedTo.id = :topographeId) AND " +
            "(:specialties IS NULL OR LOWER(t.specialties) LIKE LOWER(CONCAT('%', :specialties, '%')))")
    Page<Technicien> findWithFilters(@Param("skillLevel") SkillLevel skillLevel,
                                     @Param("cityName") String cityName,
                                     @Param("isActive") Boolean isActive,
                                     @Param("topographeId") Long topographeId,
                                     @Param("specialties") String specialties,
                                     Pageable pageable);

    // Techniciens par topographe
    Page<Technicien> findByAssignedToId(Long topographeId, Pageable pageable);

    // Techniciens disponibles (actifs et avec peu de tâches)
    @Query("SELECT t FROM Technicien t WHERE t.isActive = true AND " +
            "(SELECT COUNT(task) FROM Task task WHERE task.assignedTechnicien = t AND task.status IN ('TODO', 'IN_PROGRESS')) < :maxTasks")
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
            "LEFT JOIN t.tasks task " +
            "WHERE t.isActive = true " +
            "GROUP BY t " +
            "ORDER BY taskCount DESC")
    List<Object[]> findTechniciensWithTaskCount(Pageable pageable);

    @Query("SELECT COUNT(t) FROM Technicien t WHERE t.assignedTo.id = :topographeId")
    long countByAssignedToId(@Param("topographeId") Long topographeId);
}