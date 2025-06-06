package com.topographe.topographe.repository;

import com.topographe.topographe.entity.Topographe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopographeRepository extends JpaRepository<Topographe, Long> {

    Optional<Topographe> findByUsername(String username);
    Optional<Topographe> findByEmail(String email);
    Optional<Topographe> findByCin(String cin);
    Optional<Topographe> findByPhoneNumber(String phoneNumber);
    Optional<Topographe> findByLicenseNumber(String licenseNumber);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByCin(String cin);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByLicenseNumber(String licenseNumber);

    // Recherche avec filtres - Version corrigée pour PostgreSQL
    @Query("SELECT t FROM Topographe t WHERE " +
            "(:specialization IS NULL OR :specialization = '' OR CAST(t.specialization AS string) LIKE CONCAT('%', CAST(:specialization AS string), '%')) AND " +
            "(:cityName IS NULL OR :cityName = '' OR CAST(t.city.name AS string) LIKE CONCAT('%', CAST(:cityName AS string), '%')) AND " +
            "(:isActive IS NULL OR t.isActive = :isActive)")
    Page<Topographe> findWithFilters(@Param("specialization") String specialization,
                                     @Param("cityName") String cityName,
                                     @Param("isActive") Boolean isActive,
                                     Pageable pageable);

    // Alternative avec requête SQL native si la requête JPQL ne fonctionne pas
    @Query(value = "SELECT * FROM users u " +
            "JOIN city c ON c.id = u.city_id " +
            "WHERE u.user_type = 'TOPOGRAPHE' " +
            "AND (:specialization IS NULL OR :specialization = '' OR CAST(u.specialization AS TEXT) ILIKE CONCAT('%', :specialization, '%')) " +
            "AND (:cityName IS NULL OR :cityName = '' OR CAST(c.name AS TEXT) ILIKE CONCAT('%', :cityName, '%')) " +
            "AND (:isActive IS NULL OR u.is_active = :isActive)",
            countQuery = "SELECT COUNT(*) FROM users u " +
                    "JOIN city c ON c.id = u.city_id " +
                    "WHERE u.user_type = 'TOPOGRAPHE' " +
                    "AND (:specialization IS NULL OR :specialization = '' OR CAST(u.specialization AS TEXT) ILIKE CONCAT('%', :specialization, '%')) " +
                    "AND (:cityName IS NULL OR :cityName = '' OR CAST(c.name AS TEXT) ILIKE CONCAT('%', :cityName, '%')) " +
                    "AND (:isActive IS NULL OR u.is_active = :isActive)",
            nativeQuery = true)
    Page<Topographe> findWithFiltersNative(@Param("specialization") String specialization,
                                           @Param("cityName") String cityName,
                                           @Param("isActive") Boolean isActive,
                                           Pageable pageable);

    // Topographes actifs
    List<Topographe> findByIsActiveTrue();

    // Statistiques
    @Query("SELECT COUNT(t) FROM Topographe t WHERE t.isActive = true")
    long countActiveTopographes();
}
