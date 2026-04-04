package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import t.esprit.arctic.jobmatch.dto.EvenementRequest;
import t.esprit.arctic.jobmatch.dto.EvenementResponse;
import t.esprit.arctic.jobmatch.entity.Evenement;
import t.esprit.arctic.jobmatch.entity.OrganisateurEvenement;
import t.esprit.arctic.jobmatch.repository.EvenementRepository;
import t.esprit.arctic.jobmatch.repository.OrganisateurEvenementRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvenementService {

    private final EvenementRepository repository;
    private final OrganisateurEvenementRepository organisateurRepository;

    // ================= CREATE =================
    public EvenementResponse publier(EvenementRequest request) {

        // ✅ Récupère par ID directement
        OrganisateurEvenement organisateur = organisateurRepository
                .findById(request.getOrganisateurId())
                .orElseThrow(() -> new RuntimeException("Organisateur non trouvé : " + request.getOrganisateurId()));

        Evenement e = Evenement.builder()
                .titre(request.getTitre())
                .date(request.getDate())
                .lieu(request.getLieu())
                .type(request.getType())
                .organisateur(organisateur)
                .build();

        return toResponse(repository.save(e));
    }

    // ================= UPDATE =================
    public EvenementResponse modifier(Long id, EvenementRequest request, String email) {
        Evenement e = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé : " + id));

        OrganisateurEvenement organisateur = organisateurRepository
                .findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Organisateur non trouvé"));


        if (!e.getOrganisateur().getId().equals(organisateur.getId())) {
            throw new RuntimeException("Accès refusé : vous n'êtes pas le propriétaire de cet événement");
        }

        e.setTitre(request.getTitre());
        e.setDate(request.getDate());
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


        if (!e.getOrganisateur().getId().equals(organisateur.getId())) {
            throw new RuntimeException("Accès refusé : vous n'êtes pas le propriétaire de cet événement");
        }

        repository.delete(e);
    }



    // GET ALL
    public List<EvenementResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // GET BY ID
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
                e.getDate(),
                e.getLieu(),
                e.getType(),
                e.getOrganisateur() != null ? e.getOrganisateur().getId() : null,   // ✅
                e.getOrganisateur() != null ? e.getOrganisateur().getNom() : null   // ✅ a changer
        );
    }

    // Suppression admin — sans vérifier l'organisateur
    public void annulerAdmin(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Événement non trouvé : " + id);
        }
        repository.deleteById(id);
    }
}