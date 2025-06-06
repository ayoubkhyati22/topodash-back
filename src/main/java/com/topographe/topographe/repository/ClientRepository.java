package com.topographe.topographe.repository;

import com.topographe.topographe.entity.Client;
import com.topographe.topographe.entity.enumm.ClientType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByUsername(String username);
    Optional<Client> findByEmail(String email);
    Optional<Client> findByCin(String cin);
    Optional<Client> findByPhoneNumber(String phoneNumber);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByCin(String cin);
    boolean existsByPhoneNumber(String phoneNumber);

    // Recherche avec filtres
    @Query("SELECT c FROM Client c WHERE " +
            "(:clientType IS NULL OR c.clientType = :clientType) AND " +
            "(:cityName IS NULL OR LOWER(c.city.name) LIKE LOWER(CONCAT('%', :cityName, '%'))) AND " +
            "(:isActive IS NULL OR c.isActive = :isActive) AND " +
            "(:topographeId IS NULL OR c.createdBy.id = :topographeId) AND " +
            "(:companyName IS NULL OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', :companyName, '%')))")
    Page<Client> findWithFilters(@Param("clientType") ClientType clientType,
                                 @Param("cityName") String cityName,
                                 @Param("isActive") Boolean isActive,
                                 @Param("topographeId") Long topographeId,
                                 @Param("companyName") String companyName,
                                 Pageable pageable);

    // Clients par topographe
    Page<Client> findByCreatedById(Long topographeId, Pageable pageable);

    // Clients actifs
    List<Client> findByIsActiveTrue();

    // Clients par type
    List<Client> findByClientType(ClientType clientType);

    // Statistiques
    @Query("SELECT COUNT(c) FROM Client c WHERE c.isActive = true")
    long countActiveClients();

    @Query("SELECT COUNT(c) FROM Client c WHERE c.createdBy.id = :topographeId")
    long countByTopographeId(@Param("topographeId") Long topographeId);

    @Query("SELECT c.clientType, COUNT(c) FROM Client c GROUP BY c.clientType")
    List<Object[]> countByClientType();
}