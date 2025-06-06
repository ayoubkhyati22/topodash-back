package com.topographe.topographe.repository;

import com.topographe.topographe.entity.Task;
import com.topographe.topographe.entity.enumm.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Recherche avec filtres
    @Query("SELECT t FROM Task t WHERE " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:projectId IS NULL OR t.project.id = :projectId) AND " +
            "(:technicienId IS NULL OR t.assignedTechnicien.id = :technicienId) AND " +
            "(:topographeId IS NULL OR t.project.topographe.id = :topographeId) AND " +
            "(:clientId IS NULL OR t.project.client.id = :clientId) AND " +
            "(:dueDateFrom IS NULL OR t.dueDate >= :dueDateFrom) AND " +
            "(:dueDateTo IS NULL OR t.dueDate <= :dueDateTo) AND " +
            "(:title IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')))")
    Page<Task> findWithFilters(@Param("status") TaskStatus status,
                               @Param("projectId") Long projectId,
                               @Param("technicienId") Long technicienId,
                               @Param("topographeId") Long topographeId,
                               @Param("clientId") Long clientId,
                               @Param("dueDateFrom") LocalDate dueDateFrom,
                               @Param("dueDateTo") LocalDate dueDateTo,
                               @Param("title") String title,
                               Pageable pageable);

    // Tâches par projet
    Page<Task> findByProjectId(Long projectId, Pageable pageable);

    // Tâches par technicien
    Page<Task> findByAssignedTechnicienId(Long technicienId, Pageable pageable);

    // Tâches par topographe (via projet)
    @Query("SELECT t FROM Task t WHERE t.project.topographe.id = :topographeId")
    Page<Task> findByTopographeId(@Param("topographeId") Long topographeId, Pageable pageable);

    // Tâches par client (via projet)
    @Query("SELECT t FROM Task t WHERE t.project.client.id = :clientId")
    Page<Task> findByClientId(@Param("clientId") Long clientId, Pageable pageable);

    // Tâches non assignées
    List<Task> findByAssignedTechnicienIsNull();

    // Tâches en retard
    @Query("SELECT t FROM Task t WHERE t.dueDate < :currentDate AND t.status NOT IN ('COMPLETED')")
    List<Task> findOverdueTasks(@Param("currentDate") LocalDate currentDate);

    // Tâches à échéance proche
    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :startDate AND :endDate AND t.status NOT IN ('COMPLETED')")
    List<Task> findTasksDueSoon(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Tâches actives (TODO + IN_PROGRESS + REVIEW)
    @Query("SELECT t FROM Task t WHERE t.status IN ('TODO', 'IN_PROGRESS', 'REVIEW')")
    List<Task> findActiveTasks();

    // Tâches par statut
    List<Task> findByStatus(TaskStatus status);

    // Statistiques
    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = :status")
    long countByStatus(@Param("status") TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId")
    long countByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTechnicien.id = :technicienId")
    long countByTechnicienId(@Param("technicienId") Long technicienId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.topographe.id = :topographeId")
    long countByTopographeId(@Param("topographeId") Long topographeId);

    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> countByStatusGrouped();

    // Charge de travail par technicien
    @Query("SELECT t.assignedTechnicien.id, COUNT(t) FROM Task t " +
            "WHERE t.assignedTechnicien IS NOT NULL AND t.status IN ('TODO', 'IN_PROGRESS', 'REVIEW') " +
            "GROUP BY t.assignedTechnicien.id")
    List<Object[]> getWorkloadByTechnicien();

    // Tâches par priorité (basée sur la date d'échéance)
    @Query("SELECT t FROM Task t WHERE t.dueDate IS NOT NULL AND t.status NOT IN ('COMPLETED') " +
            "ORDER BY t.dueDate ASC")
    List<Task> findTasksByPriority(Pageable pageable);

    // Performance par technicien (tâches terminées)
    @Query("SELECT t.assignedTechnicien.id, COUNT(t) FROM Task t " +
            "WHERE t.assignedTechnicien IS NOT NULL AND t.status = 'COMPLETED' " +
            "AND t.createdAt >= :startDate " +
            "GROUP BY t.assignedTechnicien.id " +
            "ORDER BY COUNT(t) DESC")
    List<Object[]> getTechnicienPerformance(@Param("startDate") LocalDate startDate, Pageable pageable);

    // Tâches créées dans une période
    @Query("SELECT t FROM Task t WHERE t.createdAt >= :startDate AND t.createdAt <= :endDate")
    List<Task> findTasksCreatedInPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}