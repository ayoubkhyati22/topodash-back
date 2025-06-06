package com.topographe.topographe.controller;

import com.topographe.topographe.dto.request.ClientCreateRequest;
import com.topographe.topographe.dto.request.ClientUpdateRequest;
import com.topographe.topographe.dto.response.ApiResponse;
import com.topographe.topographe.dto.response.PageResponse;
import com.topographe.topographe.dto.response.ClientResponse;
import com.topographe.topographe.entity.enumm.ClientType;
import com.topographe.topographe.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<ClientResponse>> createClient(
            @Valid @RequestBody ClientCreateRequest request) {
        ClientResponse clientResponse = clientService.createClient(request);
        ApiResponse<ClientResponse> response = new ApiResponse<>(
                "Client créé avec succès",
                clientResponse,
                HttpStatus.CREATED.value()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ClientResponse>>> getAllClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PageResponse<ClientResponse> pageResponse = clientService.getAllClients(page, size, sortBy, sortDir);
        ApiResponse<PageResponse<ClientResponse>> response = new ApiResponse<>(
                "Liste des clients récupérée avec succès",
                pageResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ClientResponse>>> searchClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) ClientType clientType,
            @RequestParam(required = false) String cityName,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Long topographeId,
            @RequestParam(required = false) String companyName) {

        PageResponse<ClientResponse> pageResponse = clientService.getClientsWithFilters(
                page, size, sortBy, sortDir, clientType, cityName, isActive, topographeId, companyName);

        ApiResponse<PageResponse<ClientResponse>> response = new ApiResponse<>(
                "Recherche de clients effectuée avec succès",
                pageResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/topographe/{topographeId}")
    public ResponseEntity<ApiResponse<PageResponse<ClientResponse>>> getClientsByTopographe(
            @PathVariable Long topographeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PageResponse<ClientResponse> pageResponse = clientService.getClientsByTopographe(
                topographeId, page, size, sortBy, sortDir);

        ApiResponse<PageResponse<ClientResponse>> response = new ApiResponse<>(
                "Clients du topographe récupérés avec succès",
                pageResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientResponse>> getClientById(@PathVariable Long id) {
        ClientResponse clientResponse = clientService.getClientById(id);
        ApiResponse<ClientResponse> response = new ApiResponse<>(
                "Client trouvé avec succès",
                clientResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientResponse>> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientUpdateRequest request) {

        ClientResponse clientResponse = clientService.updateClient(id, request);
        ApiResponse<ClientResponse> response = new ApiResponse<>(
                "Client mis à jour avec succès",
                clientResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        ApiResponse<String> response = new ApiResponse<>(
                "Client supprimé avec succès",
                null,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<String>> activateClient(@PathVariable Long id) {
        clientService.activateClient(id);
        ApiResponse<String> response = new ApiResponse<>(
                "Client activé avec succès",
                null,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateClient(@PathVariable Long id) {
        clientService.deactivateClient(id);
        ApiResponse<String> response = new ApiResponse<>(
                "Client désactivé avec succès",
                null,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats/total")
    public ResponseEntity<ApiResponse<Long>> getTotalActiveClients() {
        long total = clientService.getTotalActiveClients();
        ApiResponse<Long> response = new ApiResponse<>(
                "Nombre total de clients actifs récupéré avec succès",
                total,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats/topographe/{topographeId}")
    public ResponseEntity<ApiResponse<Long>> getClientCountByTopographe(@PathVariable Long topographeId) {
        long count = clientService.getClientCountByTopographe(topographeId);
        ApiResponse<Long> response = new ApiResponse<>(
                "Nombre de clients du topographe récupéré avec succès",
                count,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }
}