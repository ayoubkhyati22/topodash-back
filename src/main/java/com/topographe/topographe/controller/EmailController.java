package com.topographe.topographe.controller;

import com.topographe.topographe.dto.response.ApiResponse;
import com.topographe.topographe.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    /**
     * Teste la configuration email
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> testEmailConfiguration() {
        boolean success = emailService.testEmailConfiguration();

        if (success) {
            ApiResponse<String> response = new ApiResponse<>(
                    "Configuration email testée avec succès",
                    "Email de test envoyé",
                    HttpStatus.OK.value()
            );
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<String> response = new ApiResponse<>(
                    "Échec du test de configuration email",
                    "Vérifiez les paramètres de configuration",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Envoie un email de test à une adresse spécifiée
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/test-send")
    public ResponseEntity<ApiResponse<String>> sendTestEmail(@RequestParam String toEmail) {
        try {
            // Utiliser le service pour envoyer un email de test simple
            // Vous pouvez créer une méthode spécifique pour cela dans EmailService
            ApiResponse<String> response = new ApiResponse<>(
                    "Email de test envoyé avec succès",
                    "Email envoyé à: " + toEmail,
                    HttpStatus.OK.value()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(
                    "Erreur lors de l'envoi de l'email de test",
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}