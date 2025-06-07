package com.topographe.topographe.service;

import com.topographe.topographe.entity.Topographe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from:noreply@topographe.com}")
    private String fromEmail;

    @Value("${app.mail.support:support@topographe.com}")
    private String supportEmail;

    @Value("${app.name:TopoDash}")
    private String appName;

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    /**
     * Envoie un email de bienvenue avec les identifiants de connexion
     */
    public void sendWelcomeEmailToTopographe(Topographe topographe, String password) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(topographe.getEmail());
            helper.setSubject("Bienvenue sur " + appName + " - Vos identifiants de connexion");

            // Préparer le contexte pour le template
            Context context = new Context();
            context.setVariable("topographe", topographe);
            context.setVariable("username", topographe.getUsername());
            context.setVariable("password", password);
            context.setVariable("appName", appName);
            context.setVariable("appUrl", appUrl);
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("createdDate", topographe.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));

            // Générer le contenu HTML
            String htmlContent = templateEngine.process("emails/welcome-topographe", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email de bienvenue envoyé avec succès à: {}", topographe.getEmail());

        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email de bienvenue à: {}", topographe.getEmail(), e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email de bienvenue", e);
        }
    }

    /**
     * Envoie un email simple de bienvenue (fallback sans template)
     */
    public void sendSimpleWelcomeEmail(Topographe topographe, String password) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(topographe.getEmail());
            message.setSubject("Bienvenue sur " + appName + " - Vos identifiants de connexion");

            String content = buildSimpleWelcomeContent(topographe, password);
            message.setText(content);

            mailSender.send(message);
            log.info("Email simple de bienvenue envoyé avec succès à: {}", topographe.getEmail());

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email simple à: {}", topographe.getEmail(), e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }

    /**
     * Construit le contenu texte simple de l'email de bienvenue
     */
    private String buildSimpleWelcomeContent(Topographe topographe, String password) {
        return String.format("""
            Bonjour %s %s,
            
            Bienvenue sur %s !
            
            Votre compte topographe a été créé avec succès. Voici vos identifiants de connexion :
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            📧 Nom d'utilisateur : %s
            🔐 Mot de passe : %s
            🌐 URL de connexion : %s
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            IMPORTANT - Sécurité de votre compte :
            ⚠️  Changez votre mot de passe lors de votre première connexion
            ⚠️  Ne partagez jamais vos identifiants
            ⚠️  Déconnectez-vous toujours après utilisation
            
            Informations de votre profil :
            • Nom complet : %s %s
            • Email : %s
            • Téléphone : %s
            • Numéro de licence : %s
            • Spécialisation : %s
            
            Vous pouvez maintenant :
            ✅ Gérer vos clients
            ✅ Superviser vos techniciens
            ✅ Suivre vos projets
            ✅ Gérer les tâches
            
            Si vous rencontrez des difficultés, n'hésitez pas à contacter notre support :
            📧 %s
            
            Cordialement,
            L'équipe %s
            
            ═══════════════════════════════════════════════════════════════════════
            Ce message est confidentiel. Si vous l'avez reçu par erreur, 
            veuillez le supprimer et nous en informer immédiatement.
            ═══════════════════════════════════════════════════════════════════════
            """,
                topographe.getFirstName(), topographe.getLastName(),
                appName,
                topographe.getUsername(),
                password,
                appUrl + "/login",
                topographe.getFirstName(), topographe.getLastName(),
                topographe.getEmail(),
                topographe.getPhoneNumber(),
                topographe.getLicenseNumber(),
                topographe.getSpecialization(),
                supportEmail,
                appName
        );
    }

    /**
     * Envoie un email de notification à l'administrateur
     */
    public void sendAdminNotification(Topographe topographe, String adminEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(adminEmail);
            message.setSubject("[" + appName + "] Nouveau topographe créé - " + topographe.getFirstName() + " " + topographe.getLastName());

            String content = String.format("""
                Notification de création de compte
                
                Un nouveau topographe a été créé dans le système :
                
                • Nom : %s %s
                • Email : %s
                • Nom d'utilisateur : %s
                • Numéro de licence : %s
                • Spécialisation : %s
                • Date de création : %s
                
                L'email de bienvenue avec les identifiants a été envoyé à : %s
                
                L'équipe %s
                """,
                    topographe.getFirstName(), topographe.getLastName(),
                    topographe.getEmail(),
                    topographe.getUsername(),
                    topographe.getLicenseNumber(),
                    topographe.getSpecialization(),
                    topographe.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                    topographe.getEmail(),
                    appName
            );

            message.setText(content);
            mailSender.send(message);
            log.info("Notification admin envoyée avec succès pour le topographe: {}", topographe.getEmail());

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de la notification admin pour: {}", topographe.getEmail(), e);
            // Ne pas faire échouer la création si la notification admin échoue
        }
    }

    /**
     * Teste la configuration email
     */
    public boolean testEmailConfiguration() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(fromEmail);
            message.setSubject("Test configuration email - " + appName);
            message.setText("Test de configuration email réussi.");

            mailSender.send(message);
            log.info("Test de configuration email réussi");
            return true;
        } catch (Exception e) {
            log.error("Échec du test de configuration email", e);
            return false;
        }
    }
}