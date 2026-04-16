package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import t.esprit.arctic.jobmatch.dto.EvenementRequest;
import t.esprit.arctic.jobmatch.dto.EvenementResponse;
import t.esprit.arctic.jobmatch.dto.EvenementStatsResponse;
import t.esprit.arctic.jobmatch.entity.Evenement;
import t.esprit.arctic.jobmatch.entity.OrganisateurEvenement;
import t.esprit.arctic.jobmatch.repository.EvenementRepository;
import t.esprit.arctic.jobmatch.repository.OrganisateurEvenementRepository;
import t.esprit.arctic.jobmatch.repository.ParticipationRepository;

<<<<<<< HEAD

=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvenementService {

    private final EvenementRepository repository;
    private final OrganisateurEvenementRepository organisateurRepository;
    private final ParticipationRepository participationRepository;

<<<<<<< HEAD
    // ================= CREATE =================
    public EvenementResponse publier(EvenementRequest request) {

        // ✅ Récupère par ID directement
=======

    public EvenementResponse publier(EvenementRequest request) {
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
        OrganisateurEvenement organisateur = organisateurRepository
                .findById(request.getOrganisateurId())
                .orElseThrow(() -> new RuntimeException("Organisateur non trouvé : " + request.getOrganisateurId()));

        Evenement e = Evenement.builder()
                .titre(request.getTitre())
<<<<<<< HEAD
                .date(request.getDate())
=======
                .dateHeure(request.getDateHeure())
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
                .lieu(request.getLieu())
                .type(request.getType())
                .organisateur(organisateur)
                .build();

        return toResponse(repository.save(e));
    }

<<<<<<< HEAD
    // ================= UPDATE =================
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    public EvenementResponse modifier(Long id, EvenementRequest request, String email) {
        Evenement e = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé : " + id));

        OrganisateurEvenement organisateur = organisateurRepository
                .findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Organisateur non trouvé"));

<<<<<<< HEAD

=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
        if (!e.getOrganisateur().getId().equals(organisateur.getId())) {
            throw new RuntimeException("Accès refusé : vous n'êtes pas le propriétaire de cet événement");
        }

        e.setTitre(request.getTitre());
<<<<<<< HEAD
        e.setDate(request.getDate());
=======
        e.setDateHeure(request.getDateHeure());
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
        e.setLieu(request.getLieu());
        e.setType(request.getType());

        return toResponse(repository.save(e));
    }

    public void annuler(Long id, String email) {
        Evenement e = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé : " + id));

        OrganisateurEvenement organisateur = organisateurRepository
                .findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Organisateur non trouvé"));

<<<<<<< HEAD

=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
        if (!e.getOrganisateur().getId().equals(organisateur.getId())) {
            throw new RuntimeException("Accès refusé : vous n'êtes pas le propriétaire de cet événement");
        }

        repository.delete(e);
    }


<<<<<<< HEAD

    // GET ALL
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    public List<EvenementResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

<<<<<<< HEAD
    // GET BY ID
=======

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    public EvenementResponse getById(Long id) {
        return toResponse(repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé : " + id)));
    }

    public List<EvenementResponse> getByOrganisateur(Long organisateurId) {
        return repository.findByOrganisateurId(organisateurId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ================= MAPPER =================
    private EvenementResponse toResponse(Evenement e) {
        return new EvenementResponse(
                e.getId(),
                e.getTitre(),
<<<<<<< HEAD
                e.getDate(),
                e.getLieu(),
                e.getType(),
                e.getOrganisateur() != null ? e.getOrganisateur().getId() : null,   // ✅
                e.getOrganisateur() != null ? e.getOrganisateur().getNom() : null   // ✅ a changer
        );
    }

    // Suppression admin — sans vérifier l'organisateur
=======
                e.getDateHeure(),
                e.getLieu(),
                e.getType(),
                e.getOrganisateur() != null ? e.getOrganisateur().getId() : null,
                e.getOrganisateur() != null ? e.getOrganisateur().getNom() : null,
                e.isChatOuvert()
        );
    }

    // Suppression admin
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    public void annulerAdmin(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Événement non trouvé : " + id);
        }
        repository.deleteById(id);
    }

<<<<<<< HEAD
    // Calcule les statistiques d'un organisateur pour un mois donné
    public EvenementStatsResponse getStats(int mois, int annee, Long organisateurId) {

        // 1. Nombre total d'événements ce mois
        int totalEvenements = repository
                .countByMoisAndAnneeAndOrganisateur(mois, annee, organisateurId);

        // 2. Récupère les événements pour calculer les participations
        List<Evenement> evenements = repository
                .findByMoisAndAnneeAndOrganisateur(mois, annee, organisateurId);

        // 3. Total participations confirmées
        int totalConfirmees = participationRepository
                .countByOrganisateurAndMoisAndStatut(organisateurId, mois, annee, "CONFIRME");

        // 4. Total participations en attente
        int totalEnAttente = participationRepository
                .countByOrganisateurAndMoisAndStatut(organisateurId, mois, annee, "EN_ATTENTE");

        // 5. Total participations
        int totalParticipations = totalConfirmees + totalEnAttente;

        // 6. Taux de remplissage moyen
=======
    // Stats
    public EvenementStatsResponse getStats(int mois, int annee, Long organisateurId) {
        int totalEvenements = repository
                .countByMoisAndAnneeAndOrganisateur(mois, annee, organisateurId);

        List<Evenement> evenements = repository
                .findByMoisAndAnneeAndOrganisateur(mois, annee, organisateurId);

        int totalConfirmees = participationRepository
                .countByOrganisateurAndMoisAndStatut(organisateurId, mois, annee, "CONFIRME");

        int totalEnAttente = participationRepository
                .countByOrganisateurAndMoisAndStatut(organisateurId, mois, annee, "EN_ATTENTE");

        int totalParticipations = totalConfirmees + totalEnAttente;

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
        double tauxRemplissage = totalEvenements > 0
                ? (double) totalConfirmees / totalEvenements
                : 0.0;

<<<<<<< HEAD
        // 7. Événement le plus populaire
        String evenementPopulaire = "Aucun";
        int maxParticipations = 0;

        for (Evenement e : evenements) {
            int nbParticipations = participationRepository.countByEvenementId(e.getId());
            if (nbParticipations > maxParticipations) {
                maxParticipations = nbParticipations;
                evenementPopulaire = e.getTitre();
=======
        String evenementPopulaire = "Aucun";
        int maxParticipations = 0;

        for (Evenement ev : evenements) {
            int nbParticipations = participationRepository.countByEvenementId(ev.getId());
            if (nbParticipations > maxParticipations) {
                maxParticipations = nbParticipations;
                evenementPopulaire = ev.getTitre();
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
            }
        }

        return new EvenementStatsResponse(
                totalEvenements,
                totalParticipations,
                totalConfirmees,
                totalEnAttente,
                tauxRemplissage,
                evenementPopulaire,
                maxParticipations
        );
    }
}