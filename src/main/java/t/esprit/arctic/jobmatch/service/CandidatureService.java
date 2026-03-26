package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import t.esprit.arctic.jobmatch.dto.CandidatureDTO;
import t.esprit.arctic.jobmatch.entity.Candidat;
import t.esprit.arctic.jobmatch.entity.Candidature;
import t.esprit.arctic.jobmatch.repository.CandidatRepository;
import t.esprit.arctic.jobmatch.repository.CandidatureRepository;

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
        candidature.setEntreprise(dto.getEntreprise());
        candidature.setPoste(dto.getPoste());
        candidature.setLettreGeneree(dto.getLettreGeneree());

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
        return candidatureRepository.findByEntrepriseContainingIgnoreCase(entreprise)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
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
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        candidature.setEntreprise(dto.getEntreprise());
        candidature.setPoste(dto.getPoste());
        candidature.setLettreGeneree(dto.getLettreGeneree());

        return convertToDTO(candidatureRepository.save(candidature));
    }

    private CandidatureDTO convertToDTO(Candidature c) {
        CandidatureDTO dto = new CandidatureDTO();
        dto.setId(c.getId());
        dto.setDateEnvoi(c.getDateEnvoi());
        dto.setStatut(c.getStatut());
        dto.setLettreGeneree(c.getLettreGeneree());
        dto.setEntreprise(c.getEntreprise());
        dto.setPoste(c.getPoste());
        if (c.getCandidat() != null) {
            dto.setCandidatId(c.getCandidat().getId());
            dto.setCandidatNom(c.getCandidat().getNom());
        }
        return dto;
    }
}