package com.topographe.topographe.repository;

import com.topographe.topographe.entity.Project;
import com.topographe.topographe.entity.enumm.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Recherche avec filtres
    @Query("SELECT p FROM Project p WHERE " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:clientId IS NULL OR p.client.id = :clientId) AND " +
            "(:topographeId IS NULL OR p.topographe.id = :topographeId) AND " +
            "(:startDate IS NULL OR p.startDate >= :startDate) AND " +
            "(:endDate IS NULL OR p.endDate <= :endDate) AND " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Project> findWithFilters(@Param("status") ProjectStatus status,
                                  @Param("clientId") Long clientId,
                                  @Param("topographeId") Long topographeId,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate,
                                  @Param("name") String name,
                                  Pageable pageable);

    // Projets par client
    Page<Project> findByClientId(Long clientId, Pageable pageable);

    // Projets par topographe
    Page<Project> findByTopographeId(Long topographeId, Pageable pageable);

    // Projets par statut
    List<Project> findByStatus(ProjectStatus status);

    // Projets en retard
    @Query("SELECT p FROM Project p WHERE p.endDate < :currentDate AND p.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Project> findOverdueProjects(@Param("currentDate") LocalDate currentDate);

    // Projets se terminant bientôt
    @Query("SELECT p FROM Project p WHERE p.endDate BETWEEN :startDate AND :endDate AND p.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Project> findProjectsEndingSoon(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Projets actifs (en cours ou planifiés)
    @Query("SELECT p FROM Project p WHERE p.status IN ('PLANNING', 'IN_PROGRESS')")
    List<Project> findActiveProjects();

    // Statistiques
    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status")
    long countByStatus(@Param("status") ProjectStatus status);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.client.id = :clientId")
    long countByClientId(@Param("clientId") Long clientId);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.topographe.id = :topographeId")
    long countByTopographeId(@Param("topographeId") Long topographeId);

    @Query("SELECT p.status, COUNT(p) FROM Project p GROUP BY p.status")
    List<Object[]> countByStatusGrouped();

    // Projets par période
    @Query("SELECT p FROM Project p WHERE p.startDate BETWEEN :startDate AND :endDate")
    List<Project> findProjectsByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Top projets par nombre de tâches
    @Query("SELECT p, COUNT(t) as taskCount FROM Project p " +
            "LEFT JOIN p.tasks t " +
            "GROUP BY p " +
            "ORDER BY taskCount DESC")
    List<Object[]> findProjectsWithTaskCount(Pageable pageable);
}