package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import t.esprit.arctic.jobmatch.dto.CandidatureDTO;
import t.esprit.arctic.jobmatch.entity.Candidat;
import t.esprit.arctic.jobmatch.entity.Candidature;
import t.esprit.arctic.jobmatch.repository.CandidatRepository;
import t.esprit.arctic.jobmatch.repository.CandidatureRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CandidatureService implements ICandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final CandidatRepository candidatRepository;

    @Override
    public CandidatureDTO creerCandidature(CandidatureDTO dto) {
        Candidat candidat = candidatRepository.findById(dto.getCandidatId())
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        Candidature candidature = new Candidature();
        candidature.setDateEnvoi(new Date());
        candidature.setStatut("EN_ATTENTE");
        candidature.setCandidat(candidat);
        candidature.setNomComplet(dto.getNomComplet());
        candidature.setEmail(dto.getEmail());
        candidature.setTelephone(dto.getTelephone());
        candidature.setDescription(dto.getDescription());
        candidature.setFormation(dto.getFormation());
        candidature.setExperience(dto.getExperience());
        candidature.setCompetences(dto.getCompetences());
        candidature.setLettreMotivation(dto.getLettreMotivation());
        candidature.setDateDisponibilite(dto.getDateDisponibilite());
        candidature.setPreavis(dto.getPreavis());
        candidature.setAcceptContact(dto.getAcceptContact());
        candidature.setAcceptRGPD(dto.isAcceptRGPD());

        return convertToDTO(candidatureRepository.save(candidature));
    }

    @Override
    public CandidatureDTO getCandidatureById(Long id) {
        return convertToDTO(candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée")));
    }

    @Override
    public List<CandidatureDTO> getCandidaturesByCandidat(Long candidatId) {
        return candidatureRepository.findByCandidatId(candidatId)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public CandidatureDTO modifierStatut(Long id, String statut) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));
        candidature.setStatut(statut);
        return convertToDTO(candidatureRepository.save(candidature));
    }

    @Override
    public void supprimerCandidature(Long id) {
        candidatureRepository.deleteById(id);
    }

    @Override
    public List<CandidatureDTO> rechercherParEntreprise(String entreprise) {
        // Temporairement, retourne une liste vide
        // À implémenter plus tard
        return new ArrayList<>();
    }

    @Override
    public List<CandidatureDTO> filtrerParStatut(String statut) {
        return candidatureRepository.findByStatut(statut)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<CandidatureDTO> trierParDate() {
        return candidatureRepository.findAllByOrderByDateEnvoiDesc()
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public CandidatureDTO modifierCandidature(Long id, CandidatureDTO dto) {
        // Log pour déboguer
        System.out.println("=== MODIFICATION CANDIDATURE ===");
        System.out.println("ID: " + id);
        System.out.println("DTO reçu: " + dto);

        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        // Mettre à jour TOUS les champs modifiables
        if (dto.getNomComplet() != null) {
            candidature.setNomComplet(dto.getNomComplet());
            System.out.println("Nom complet mis à jour: " + dto.getNomComplet());
        }

        if (dto.getEmail() != null) {
            candidature.setEmail(dto.getEmail());
            System.out.println("Email mis à jour: " + dto.getEmail());
        }

        if (dto.getTelephone() != null) {
            candidature.setTelephone(dto.getTelephone());
            System.out.println("Téléphone mis à jour: " + dto.getTelephone());
        }

        if (dto.getFormation() != null) {
            candidature.setFormation(dto.getFormation());
            System.out.println("Formation mise à jour: " + dto.getFormation());
        }

        if (dto.getExperience() != null) {
            candidature.setExperience(dto.getExperience());
            System.out.println("Expérience mise à jour: " + dto.getExperience());
        }

        if (dto.getCompetences() != null) {
            candidature.setCompetences(dto.getCompetences());
            System.out.println("Compétences mises à jour: " + dto.getCompetences());
        }

        if (dto.getLettreMotivation() != null) {
            candidature.setLettreMotivation(dto.getLettreMotivation());
            System.out.println("Lettre de motivation mise à jour: " + dto.getLettreMotivation());
        }

        if (dto.getDateDisponibilite() != null) {
            candidature.setDateDisponibilite(dto.getDateDisponibilite());
            System.out.println("Date disponibilité mise à jour: " + dto.getDateDisponibilite());
        }

        if (dto.getPreavis() != null) {
            candidature.setPreavis(dto.getPreavis());
            System.out.println("Préavis mis à jour: " + dto.getPreavis());
        }

        if (dto.getDescription() != null) {
            candidature.setDescription(dto.getDescription());
            System.out.println("Description mise à jour: " + dto.getDescription());
        }

        if (dto.getAcceptContact() != null) {
            candidature.setAcceptContact(dto.getAcceptContact());
            System.out.println("Accept contact mis à jour: " + dto.getAcceptContact());
        }

        candidature.setAcceptRGPD(dto.isAcceptRGPD());

        // Sauvegarder les modifications
        Candidature updated = candidatureRepository.save(candidature);
        System.out.println(" Candidature sauvegardée avec succès");

        return convertToDTO(updated);
    }

    private CandidatureDTO convertToDTO(Candidature c) {
        CandidatureDTO dto = new CandidatureDTO();
        dto.setId(c.getId());
        dto.setDateEnvoi(c.getDateEnvoi());
        dto.setStatut(c.getStatut());
        dto.setLettreGeneree(c.getLettreGeneree());
        dto.setNomComplet(c.getNomComplet());
        dto.setEmail(c.getEmail());
        dto.setTelephone(c.getTelephone());
        dto.setDescription(c.getDescription());
        dto.setFormation(c.getFormation());
        dto.setExperience(c.getExperience());
        dto.setCompetences(c.getCompetences());
        dto.setLettreMotivation(c.getLettreMotivation());
        dto.setDateDisponibilite(c.getDateDisponibilite());
        dto.setPreavis(c.getPreavis());
        dto.setAcceptContact(c.getAcceptContact());
        dto.setAcceptRGPD(c.getAcceptRGPD());

        if (c.getCandidat() != null) {
            dto.setCandidatId(c.getCandidat().getId());
            dto.setCandidatNom(c.getCandidat().getNom());
        }
        return dto;
    }
}