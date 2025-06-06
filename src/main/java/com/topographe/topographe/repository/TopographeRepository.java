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

    // Recherche avec filtres
    @Query("SELECT t FROM Topographe t WHERE " +
            "(:specialization IS NULL OR LOWER(t.specialization) LIKE LOWER(CONCAT('%', :specialization, '%'))) AND " +
            "(:cityName IS NULL OR LOWER(t.city.name) LIKE LOWER(CONCAT('%', :cityName, '%'))) AND " +
            "(:isActive IS NULL OR t.isActive = :isActive)")
    Page<Topographe> findWithFilters(@Param("specialization") String specialization,
                                     @Param("cityName") String cityName,
                                     @Param("isActive") Boolean isActive,
                                     Pageable pageable);

    // Topographes actifs
    List<Topographe> findByIsActiveTrue();

    // Statistiques
    @Query("SELECT COUNT(t) FROM Topographe t WHERE t.isActive = true")
    long countActiveTopographes();
}