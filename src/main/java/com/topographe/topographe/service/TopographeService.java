package com.topographe.topographe.service;

import com.topographe.topographe.dto.request.TopographeCreateRequest;
import com.topographe.topographe.dto.request.TopographeUpdateRequest;
import com.topographe.topographe.dto.response.TopographeResponse;
import com.topographe.topographe.dto.response.UserPageResponse;

public interface TopographeService {

    TopographeResponse createTopographe(TopographeCreateRequest request);

    UserPageResponse<TopographeResponse> getAllTopographes(int page, int size, String sortBy, String sortDir);

    UserPageResponse<TopographeResponse> getTopographesWithFilters(
            int page, int size, String sortBy, String sortDir,
            String specialization, String cityName, Boolean isActive
    );

    TopographeResponse getTopographeById(Long id);

    TopographeResponse updateTopographe(Long id, TopographeUpdateRequest request);

    void deleteTopographe(Long id);

    void activateTopographe(Long id);

    void deactivateTopographe(Long id);
}