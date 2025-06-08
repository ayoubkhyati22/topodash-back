// TaskRepository.java - Ajout des nouvelles méthodes
package com.topographe.topographe.repository;

import com.topographe.topographe.entity.Task;
import com.topographe.topographe.entity.Technicien;
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

    // Recherche avec filtres (mise à jour pour Many-to-Many)
    @Query("SELECT DISTINCT t FROM Task t " +
            "LEFT JOIN t.assignedTechniciens tech " +
            "WHERE (:status IS NULL OR t.status = :status) AND " +
            "(:projectId IS NULL OR t.project.id = :projectId) AND " +
            "(:technicienId IS NULL OR tech.id = :technicienId) AND " +
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

    // Tâches par technicien (mise à jour pour Many-to-Many)
    @Query("SELECT DISTINCT t FROM Task t JOIN t.assignedTechniciens tech WHERE tech.id = :technicienId")
    Page<Task> findByAssignedTechnicienId(@Param("technicienId") Long technicienId, Pageable pageable);

    // Tâches non assignées (mise à jour)
    @Query("SELECT t FROM Task t WHERE t.assignedTechniciens IS EMPTY")
    List<Task> findUnassignedTasks();

    // Nouvelles méthodes pour les statistiques
    @Query("SELECT COUNT(DISTINCT t) FROM Task t JOIN t.assignedTechniciens tech WHERE tech.id = :technicienId")
    long countByTechnicienId(@Param("technicienId") Long technicienId);

    @Query("SELECT COUNT(DISTINCT t) FROM Task t JOIN t.assignedTechniciens tech " +
            "WHERE tech.id = :technicienId AND t.status = :status")
    long countByTechnicienIdAndStatus(@Param("technicienId") Long technicienId, @Param("status") TaskStatus status);

    // Méthodes pour calculer les statistiques des projets
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.status = :status")
    long countByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") TaskStatus status);

    // Charge de travail par technicien (mise à jour)
    @Query("SELECT tech.id, COUNT(DISTINCT t) FROM Task t " +
            "JOIN t.assignedTechniciens tech " +
            "WHERE t.status IN ('TODO', 'IN_PROGRESS', 'REVIEW') " +
            "GROUP BY tech.id")
    List<Object[]> getWorkloadByTechnicien();

    // Techniciens disponibles basé sur la charge
    @Query("SELECT t FROM Technicien t WHERE t.isActive = true AND " +
            "(SELECT COUNT(task) FROM Task task JOIN task.assignedTechniciens assignedTech " +
            "WHERE assignedTech = t AND task.status IN ('TODO', 'IN_PROGRESS', 'REVIEW')) < :maxTasks")
    List<Technicien> findAvailableTechniciens(@Param("maxTasks") int maxTasks);

    // Reste des méthodes existantes...
    Page<Task> findByProjectId(Long projectId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.project.topographe.id = :topographeId")
    Page<Task> findByTopographeId(@Param("topographeId") Long topographeId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.project.client.id = :clientId")
    Page<Task> findByClientId(@Param("clientId") Long clientId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.dueDate < :currentDate AND t.status NOT IN ('COMPLETED')")
    List<Task> findOverdueTasks(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :startDate AND :endDate AND t.status NOT IN ('COMPLETED')")
    List<Task> findTasksDueSoon(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM Task t WHERE t.status IN ('TODO', 'IN_PROGRESS', 'REVIEW')")
    List<Task> findActiveTasks();

    List<Task> findByStatus(TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = :status")
    long countByStatus(@Param("status") TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId")
    long countByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.topographe.id = :topographeId")
    long countByTopographeId(@Param("topographeId") Long topographeId);

    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> countByStatusGrouped();

    @Query("SELECT t FROM Task t WHERE t.dueDate IS NOT NULL AND t.status NOT IN ('COMPLETED') " +
            "ORDER BY t.dueDate ASC")
    List<Task> findTasksByPriority(Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.createdAt >= :startDate AND t.createdAt <= :endDate")
    List<Task> findTasksCreatedInPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}