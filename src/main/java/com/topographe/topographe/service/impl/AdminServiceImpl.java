package com.topographe.topographe.service.impl;

import com.topographe.topographe.dto.request.AdminCreateRequest;
import com.topographe.topographe.dto.response.AdminResponse;
import com.topographe.topographe.dto.response.PageResponse;
import com.topographe.topographe.entity.Admin;
import com.topographe.topographe.entity.referentiel.City;
import com.topographe.topographe.exception.ResourceNotFoundException;
import com.topographe.topographe.exception.DuplicateResourceException;
import com.topographe.topographe.mapper.AdminMapper;
import com.topographe.topographe.repository.AdminRepository;
import com.topographe.topographe.repository.referentiel.CityRepository;
import com.topographe.topographe.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final CityRepository cityRepository;
    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AdminResponse createAdmin(AdminCreateRequest request) {
        // Vérifier les doublons
        validateUniqueFields(request);

        // Récupérer la ville
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("Ville non trouvée avec l'ID: " + request.getCityId()));

        // Encoder le mot de passe
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // Créer l'admin
        Admin admin = adminMapper.toEntity(request, city, encodedPassword);
        Admin savedAdmin = adminRepository.save(admin);

        return adminMapper.toResponse(savedAdmin);
    }

    @Override
    public PageResponse<AdminResponse> getAllAdmins(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Admin> adminPage = adminRepository.findAll(pageable);

        List<AdminResponse> adminResponses = adminPage.getContent()
                .stream()
                .map(adminMapper::toResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                adminResponses,
                adminPage.getNumber(),
                adminPage.getSize(),
                adminPage.getTotalElements(),
                adminPage.getTotalPages(),
                adminPage.isFirst(),
                adminPage.isLast(),
                adminPage.hasNext(),
                adminPage.hasPrevious()
        );
    }

    @Override
    public AdminResponse getAdminById(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin non trouvé avec l'ID: " + id));
        return adminMapper.toResponse(admin);
    }

    private void validateUniqueFields(AdminCreateRequest request) {
        if (adminRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Un utilisateur avec ce nom d'utilisateur existe déjà");
        }
        if (adminRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Un utilisateur avec cet email existe déjà");
        }
        if (adminRepository.existsByCin(request.getCin())) {
            throw new DuplicateResourceException("Un utilisateur avec ce CIN existe déjà");
        }
        if (adminRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("Un utilisateur avec ce numéro de téléphone existe déjà");
        }
    }
}