package t.esprit.arctic.jobmatch.service;

<<<<<<< HEAD
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
=======
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

<<<<<<< HEAD
    public void envoyerCandidature(
            String emailEntreprise,

            String emailCandidat,
            String messageCandidat,
            String titreOffre,
            MultipartFile cv
    ) throws Exception {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(emailEntreprise);
        helper.setSubject("Candidature pour : " + titreOffre);
        helper.setText(
                "<div style='font-family:Arial,sans-serif; padding:20px;'>" +
                        "<h2 style='color:#6366f1;'>Nouvelle Candidature</h2>" +
                        "<hr/>" +
                        "<table style='width:100%;'>" +
                        "<tr><td><b>Offre :</b></td><td>" + titreOffre + "</td></tr>" +

                        "<tr><td><b>Email :</b></td><td>" +
                        "<a href='mailto:" + emailCandidat + "'>" + emailCandidat + "</a>" +
                        "</td></tr>" +
                        "</table>" +
                        "<hr/>" +
                        "<p><b>Message :</b></p>" +
                        "<p>" + messageCandidat + "</p>" +
                        "<hr/>" +
                        "<p style='color:#9ca3af;font-size:12px;'>Envoyé via JobMatch</p>" +
                        "</div>",
                true
        );

        if (cv != null && !cv.isEmpty()) {
            helper.addAttachment(cv.getOriginalFilename(), cv);
        }

        mailSender.send(message);
=======
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
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    }
}