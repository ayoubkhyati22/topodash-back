package com.topographe.topographe.service;

import com.topographe.topographe.dto.request.TechnicienCreateRequest;
import com.topographe.topographe.dto.request.TechnicienUpdateRequest;
import com.topographe.topographe.dto.response.PageResponse;
import com.topographe.topographe.dto.response.TechnicienResponse;
import com.topographe.topographe.entity.User;
import com.topographe.topographe.entity.enumm.SkillLevel;

import java.util.List;

public interface TechnicienService {

    TechnicienResponse createTechnicien(TechnicienCreateRequest request, User currentUser);

    PageResponse<TechnicienResponse> getAllTechniciens(int page, int size, String sortBy, String sortDir, User currentUser);

    PageResponse<TechnicienResponse> getTechniciensWithFilters(
            int page, int size, String sortBy, String sortDir,
            SkillLevel skillLevel, String cityName, Boolean isActive,
            Long topographeId, String specialties, User currentUser
    );

    PageResponse<TechnicienResponse> getTechniciensByTopographe(
            Long topographeId, int page, int size, String sortBy, String sortDir, User currentUser
    );

    TechnicienResponse getTechnicienById(Long id, User currentUser);

    TechnicienResponse updateTechnicien(Long id, TechnicienUpdateRequest request, User currentUser);

    void deleteTechnicien(Long id, User currentUser);

    void activateTechnicien(Long id, User currentUser);

    void deactivateTechnicien(Long id, User currentUser);

    TechnicienResponse reassignTechnicien(Long technicienId, Long newTopographeId, User currentUser);

    // Méthodes utilitaires
    List<TechnicienResponse> getAvailableTechniciens(int maxTasks, User currentUser);

    List<TechnicienResponse> getTechniciensBySkillLevel(SkillLevel skillLevel, User currentUser);

    // Méthodes statistiques
    long getTotalActiveTechniciens(User currentUser);

    long getTechnicienCountByTopographe(Long topographeId, User currentUser);
}