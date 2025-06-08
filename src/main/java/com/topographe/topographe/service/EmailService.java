package com.topographe.topographe.service;

import com.topographe.topographe.entity.Client;
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
     * Envoie un email de bienvenue avec les identifiants de connexion (Topographe)
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
     * Envoie un email de bienvenue avec les identifiants de connexion (Client)
     */
    public void sendWelcomeEmailToClient(Client client, String password, Topographe topographe) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(client.getEmail());
            helper.setSubject("Bienvenue sur " + appName + " - Accès client");

            // Préparer le contexte pour le template
            Context context = new Context();
            context.setVariable("client", client);
            context.setVariable("topographe", topographe);
            context.setVariable("username", client.getUsername());
            context.setVariable("password", password);
            context.setVariable("appName", appName);
            context.setVariable("appUrl", appUrl);
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("createdDate", client.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));

            // Générer le contenu HTML
            String htmlContent = templateEngine.process("emails/welcome-client", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email de bienvenue client envoyé avec succès à: {}", client.getEmail());

        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email de bienvenue client à: {}", client.getEmail(), e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email de bienvenue client", e);
        }
    }

    /**
     * Envoie un email simple de bienvenue (fallback sans template) pour Topographe
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
     * Envoie un email simple de bienvenue (fallback sans template) pour Client
     */
    public void sendSimpleWelcomeEmailToClient(Client client, String password, Topographe topographe) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(client.getEmail());
            message.setSubject("Bienvenue sur " + appName + " - Accès client");

            String content = buildSimpleWelcomeContentForClient(client, password, topographe);
            message.setText(content);

            mailSender.send(message);
            log.info("Email simple de bienvenue client envoyé avec succès à: {}", client.getEmail());

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email simple client à: {}", client.getEmail(), e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email client", e);
        }
    }

    /**
     * Construit le contenu texte simple de l'email de bienvenue pour Topographe
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
     * Construit le contenu texte simple de l'email de bienvenue pour Client
     */
    private String buildSimpleWelcomeContentForClient(Client client, String password, Topographe topographe) {
        return String.format("""
            Bonjour %s %s,
            
            Bienvenue sur %s !
            
            Votre compte client a été créé avec succès par votre topographe %s %s.
            Voici vos identifiants de connexion :
            
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
            • Type de client : %s
            • Entreprise : %s
            
            Votre topographe responsable :
            • Nom : %s %s
            • Email : %s
            • Téléphone : %s
            
            Grâce à votre compte, vous pouvez :
            ✅ Suivre l'avancement de vos projets
            ✅ Consulter les tâches en cours
            ✅ Communiquer avec votre équipe
            ✅ Accéder aux rapports et documents
            
            Si vous rencontrez des difficultés, contactez :
            📧 Support technique : %s
            📧 Votre topographe : %s
            
            Cordialement,
            L'équipe %s
            
            ═══════════════════════════════════════════════════════════════════════
            Ce message est confidentiel. Si vous l'avez reçu par erreur, 
            veuillez le supprimer et nous en informer immédiatement.
            ═══════════════════════════════════════════════════════════════════════
            """,
                client.getFirstName(), client.getLastName(),
                appName,
                topographe.getFirstName(), topographe.getLastName(),
                client.getUsername(),
                password,
                appUrl + "/login",
                client.getFirstName(), client.getLastName(),
                client.getEmail(),
                client.getPhoneNumber(),
                client.getClientType().toString(),
                client.getCompanyName() != null ? client.getCompanyName() : "N/A",
                topographe.getFirstName(), topographe.getLastName(),
                topographe.getEmail(),
                topographe.getPhoneNumber(),
                supportEmail,
                topographe.getEmail(),
                appName
        );
    }

    /**
     * Envoie une notification au topographe lors de la création d'un client
     */
    public void sendClientCreationNotificationToTopographe(Client client, Topographe topographe) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(topographe.getEmail());
            message.setSubject("[" + appName + "] Nouveau client créé - " + client.getFirstName() + " " + client.getLastName());

            String content = String.format("""
                Notification de création de client
                
                Un nouveau client a été ajouté à votre portefeuille :
                
                • Nom : %s %s
                • Email : %s
                • Nom d'utilisateur : %s
                • Téléphone : %s
                • Type de client : %s
                • Entreprise : %s
                • Date de création : %s
                
                L'email de bienvenue avec les identifiants a été envoyé à : %s
                
                Le client peut maintenant se connecter et suivre ses projets via la plateforme.
                
                L'équipe %s
                """,
                    client.getFirstName(), client.getLastName(),
                    client.getEmail(),
                    client.getUsername(),
                    client.getPhoneNumber(),
                    client.getClientType().toString(),
                    client.getCompanyName() != null ? client.getCompanyName() : "N/A",
                    client.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                    client.getEmail(),
                    appName
            );

            message.setText(content);
            mailSender.send(message);
            log.info("Notification de création client envoyée avec succès au topographe: {}", topographe.getEmail());

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de la notification au topographe pour le client: {}", client.getEmail(), e);
            // Ne pas faire échouer la création si la notification échoue
        }
    }

    /**
     * Envoie un email de notification à l'administrateur (Topographe)
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