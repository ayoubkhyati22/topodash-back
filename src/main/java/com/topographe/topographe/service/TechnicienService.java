package com.topographe.topographe.service;

import com.topographe.topographe.dto.request.TechnicienCreateRequest;
import com.topographe.topographe.dto.request.TechnicienUpdateRequest;
import com.topographe.topographe.dto.response.PageResponse;
import com.topographe.topographe.dto.response.TechnicienResponse;
import com.topographe.topographe.entity.enumm.SkillLevel;

import java.util.List;

public interface TechnicienService {

    TechnicienResponse createTechnicien(TechnicienCreateRequest request);

    PageResponse<TechnicienResponse> getAllTechniciens(int page, int size, String sortBy, String sortDir);

    PageResponse<TechnicienResponse> getTechniciensWithFilters(
            int page, int size, String sortBy, String sortDir,
            SkillLevel skillLevel, String cityName, Boolean isActive,
            Long topographeId, String specialties
    );

    PageResponse<TechnicienResponse> getTechniciensByTopographe(
            Long topographeId, int page, int size, String sortBy, String sortDir
    );

    TechnicienResponse getTechnicienById(Long id);

    TechnicienResponse updateTechnicien(Long id, TechnicienUpdateRequest request);

    void deleteTechnicien(Long id);

    void activateTechnicien(Long id);

    void deactivateTechnicien(Long id);

    TechnicienResponse reassignTechnicien(Long technicienId, Long newTopographeId);

    // Méthodes utilitaires
    List<TechnicienResponse> getAvailableTechniciens(int maxTasks);

    List<TechnicienResponse> getTechniciensBySkillLevel(SkillLevel skillLevel);

    // Méthodes statistiques
    long getTotalActiveTechniciens();

    long getTechnicienCountByTopographe(Long topographeId);
}