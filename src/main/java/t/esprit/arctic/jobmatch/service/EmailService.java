package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void envoyerConfirmationCandidature(String destinataireEmail, String candidatNom, String posteNom) {

        if (destinataireEmail == null || destinataireEmail.isEmpty()) {
            System.out.println(" Email invalide, envoi annulé");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinataireEmail);
            message.setSubject("Confirmation de votre candidature - MatchyKhedma");

            String contenu = String.format("""
                Bonjour %s,
                
                Nous avons bien reçu votre candidature pour le poste de : %s.
                
                Votre candidature a été enregistrée avec succès.
                
                 Date : %s
                
                Merci de votre confiance !
                
                Cordialement,
                L'équipe JobMatch
                """,
                    candidatNom,
                    posteNom,
                    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );

            message.setText(contenu);
            mailSender.send(message);

            System.out.println(" Email envoyé avec succès à: " + destinataireEmail);

        } catch (Exception e) {
            System.out.println(" Erreur envoi email: " + e.getMessage());
        }
    }
}