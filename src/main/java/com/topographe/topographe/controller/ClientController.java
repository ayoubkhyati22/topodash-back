package com.topographe.topographe.controller;

import com.topographe.topographe.dto.request.ClientCreateRequest;
import com.topographe.topographe.dto.request.ClientUpdateRequest;
import com.topographe.topographe.dto.response.ApiResponse;
import com.topographe.topographe.dto.response.PageResponse;
import com.topographe.topographe.dto.response.ClientResponse;
import com.topographe.topographe.entity.enumm.ClientType;
import com.topographe.topographe.service.ClientService;
import com.topographe.topographe.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final ClientService clientService;

    @PreAuthorize("hasRole('ADMIN') or hasRole('TOPOGRAPHE')")
    @PostMapping
    public ResponseEntity<ApiResponse<ClientResponse>> createClient(
            @Valid @RequestBody ClientCreateRequest request,
            Authentication authentication) {

        log.info("Creating client with username: {}", request.getUsername());

        // Récupérer l'utilisateur connecté
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        ClientResponse clientResponse = clientService.createClient(request, userDetails.getUser());
        ApiResponse<ClientResponse> response = new ApiResponse<>(
                "Client créé avec succès",
                clientResponse,
                HttpStatus.CREATED.value()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TOPOGRAPHE')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ClientResponse>>> getAllClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Authentication authentication) {

        log.info("Fetching all clients - page: {}, size: {}, sortBy: {}, sortDir: {}",
                page, size, sortBy, sortDir);

        // Récupérer l'utilisateur connecté
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        PageResponse<ClientResponse> pageResponse = clientService.getAllClients(
                page, size, sortBy, sortDir, userDetails.getUser());
        ApiResponse<PageResponse<ClientResponse>> response = new ApiResponse<>(
                "Liste des clients récupérée avec succès",
                pageResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TOPOGRAPHE')")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ClientResponse>>> searchClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String clientType,
            @RequestParam(required = false) String cityName,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Long topographeId,
            @RequestParam(required = false) String companyName,
            Authentication authentication) {

        log.info("Searching clients with filters - page: {}, size: {}, clientType: {}, cityName: {}, isActive: {}, topographeId: {}, companyName: {}",
                page, size, clientType, cityName, isActive, topographeId, companyName);

        try {
            // Récupérer l'utilisateur connecté
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // Convertir la chaîne clientType en enum si elle n'est pas null
            ClientType clientTypeEnum = null;
            if (clientType != null && !clientType.trim().isEmpty()) {
                try {
                    clientTypeEnum = ClientType.valueOf(clientType.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid clientType value: {}", clientType);
                    ApiResponse<PageResponse<ClientResponse>> errorResponse = new ApiResponse<>(
                            "Type de client invalide: " + clientType,
                            null,
                            HttpStatus.BAD_REQUEST.value()
                    );
                    return ResponseEntity.badRequest().body(errorResponse);
                }
            }

            // Nettoyer les paramètres de chaîne
            String cleanCityName = (cityName != null && !cityName.trim().isEmpty()) ? cityName.trim() : null;
            String cleanCompanyName = (companyName != null && !companyName.trim().isEmpty()) ? companyName.trim() : null;

            log.info("Processed search parameters - clientTypeEnum: {}, cleanCityName: {}, cleanCompanyName: {}",
                    clientTypeEnum, cleanCityName, cleanCompanyName);

            PageResponse<ClientResponse> pageResponse = clientService.getClientsWithFilters(
                    page, size, sortBy, sortDir, clientTypeEnum, cleanCityName, isActive,
                    topographeId, cleanCompanyName, userDetails.getUser());

            ApiResponse<PageResponse<ClientResponse>> response = new ApiResponse<>(
                    "Recherche de clients effectuée avec succès",
                    pageResponse,
                    HttpStatus.OK.value()
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error during client search", e);
            ApiResponse<PageResponse<ClientResponse>> errorResponse = new ApiResponse<>(
                    "Erreur lors de la recherche: " + e.getMessage(),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TOPOGRAPHE')")
    @GetMapping("/topographe/{topographeId}")
    public ResponseEntity<ApiResponse<PageResponse<ClientResponse>>> getClientsByTopographe(
            @PathVariable Long topographeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Authentication authentication) {

        log.info("Fetching clients for topographe: {}", topographeId);

        // Récupérer l'utilisateur connecté
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        PageResponse<ClientResponse> pageResponse = clientService.getClientsByTopographe(
                topographeId, page, size, sortBy, sortDir, userDetails.getUser());

        ApiResponse<PageResponse<ClientResponse>> response = new ApiResponse<>(
                "Clients du topographe récupérés avec succès",
                pageResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TOPOGRAPHE')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientResponse>> getClientById(
            @PathVariable Long id,
            Authentication authentication) {

        log.info("Fetching client by ID: {}", id);

        // Récupérer l'utilisateur connecté
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        ClientResponse clientResponse = clientService.getClientById(id, userDetails.getUser());
        ApiResponse<ClientResponse> response = new ApiResponse<>(
                "Client trouvé avec succès",
                clientResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TOPOGRAPHE')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientResponse>> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientUpdateRequest request,
            Authentication authentication) {

        log.info("Updating client with ID: {}", id);

        // Récupérer l'utilisateur connecté
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        ClientResponse clientResponse = clientService.updateClient(id, request, userDetails.getUser());
        ApiResponse<ClientResponse> response = new ApiResponse<>(
                "Client mis à jour avec succès",
                clientResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TOPOGRAPHE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteClient(
            @PathVariable Long id,
            Authentication authentication) {

        log.info("Deleting client with ID: {}", id);

        // Récupérer l'utilisateur connecté
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        clientService.deleteClient(id, userDetails.getUser());
        ApiResponse<String> response = new ApiResponse<>(
                "Client supprimé avec succès",
                null,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TOPOGRAPHE')")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<String>> activateClient(
            @PathVariable Long id,
            Authentication authentication) {

        log.info("Activating client with ID: {}", id);

        // Récupérer l'utilisateur connecté
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        clientService.activateClient(id, userDetails.getUser());
        ApiResponse<String> response = new ApiResponse<>(
                "Client activé avec succès",
                null,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TOPOGRAPHE')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateClient(
            @PathVariable Long id,
            Authentication authentication) {

        log.info("Deactivating client with ID: {}", id);

        // Récupérer l'utilisateur connecté
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        clientService.deactivateClient(id, userDetails.getUser());
        ApiResponse<String> response = new ApiResponse<>(
                "Client désactivé avec succès",
                null,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TOPOGRAPHE')")
    @GetMapping("/stats/total")
    public ResponseEntity<ApiResponse<Long>> getTotalActiveClients(Authentication authentication) {
        // Récupérer l'utilisateur connecté
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        long total = clientService.getTotalActiveClients(userDetails.getUser());
        ApiResponse<Long> response = new ApiResponse<>(
                "Nombre total de clients actifs récupéré avec succès",
                total,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TOPOGRAPHE')")
    @GetMapping("/stats/topographe/{topographeId}")
    public ResponseEntity<ApiResponse<Long>> getClientCountByTopographe(
            @PathVariable Long topographeId,
            Authentication authentication) {

        // Récupérer l'utilisateur connecté
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        long count = clientService.getClientCountByTopographe(topographeId, userDetails.getUser());
        ApiResponse<Long> response = new ApiResponse<>(
                "Nombre de clients du topographe récupéré avec succès",
                count,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }
}