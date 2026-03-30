package t.esprit.arctic.jobmatch.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.List;

import t.esprit.arctic.jobmatch.entity.Partenaire;
import t.esprit.arctic.jobmatch.entity.TypePartenaire;
import t.esprit.arctic.jobmatch.entity.Utilisateur;
import t.esprit.arctic.jobmatch.entity.Role;

import t.esprit.arctic.jobmatch.repository.PartenaireRepository;
import t.esprit.arctic.jobmatch.repository.UtilisateurRepository;

@Service
@RequiredArgsConstructor
public class PartenaireService {

    private final PartenaireRepository partenaireRepo;
    private final UtilisateurRepository utilisateurRepo;

    
    public List<Partenaire> getAll() {
        return partenaireRepo.findAll();
    }


    public Partenaire getById(Long id) {
        return partenaireRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Partenaire non trouvé"));
    }


    public Partenaire create(Partenaire p) {

        if (p.getUtilisateur() != null && p.getUtilisateur().getId() != null) {

            Utilisateur utilisateur = utilisateurRepo.findById(p.getUtilisateur().getId())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // 🔥 Vérifier que c'est ADMIN
            if (utilisateur.getRole() != Role.ADMIN) {
                throw new RuntimeException("L'utilisateur doit être ADMIN");
            }

            p.setUtilisateur(utilisateur);
        }

        return partenaireRepo.save(p);
    }


    public Partenaire update(Long id, Partenaire p) {

        Partenaire existing = getById(id);

        existing.setNom(p.getNom());
        existing.setEmail(p.getEmail());
        existing.setTelephone(p.getTelephone());
        existing.setType(p.getType());

        if (p.getUtilisateur() != null && p.getUtilisateur().getId() != null) {

            Utilisateur utilisateur = utilisateurRepo.findById(p.getUtilisateur().getId())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (utilisateur.getRole() != Role.ADMIN) {
                throw new RuntimeException("L'utilisateur doit être ADMIN");
            }

            existing.setUtilisateur(utilisateur);
        }

        return partenaireRepo.save(existing);
    }


    public void delete(Long id) {
        partenaireRepo.deleteById(id);
    }


    public List<Partenaire> getByType(TypePartenaire type) {
        return partenaireRepo.findByType(type);
    }
}