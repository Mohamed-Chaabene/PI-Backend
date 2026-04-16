package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import t.esprit.arctic.jobmatch.entity.Evenement;
import t.esprit.arctic.jobmatch.entity.Participation;
import t.esprit.arctic.jobmatch.entity.Utilisateur;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    // Participations par événement
    List<Participation> findByEvenementId(Long evenementId);

    // Participations par candidat
    List<Participation> findByCandidatId(Long candidatId);

    // Vérifie si candidat déjà inscrit
    boolean existsByCandidatIdAndEvenementId(Long candidatId, Long evenementId);

    //  Par statut
    List<Participation> findByEvenementIdAndStatut(Long evenementId, String statut);

    //  Toutes demandes EN_ATTENTE pour les événements d'un organisateur
    @Query("SELECT p FROM Participation p WHERE p.evenement.organisateur.id = :organisateurId AND p.statut = 'EN_ATTENTE'")
    List<Participation> findDemandesByOrganisateur(@Param("organisateurId") Long organisateurId);

    //  Compte les participations par événement et statut
    @Query("SELECT COUNT(p) FROM Participation p " +
            "WHERE p.evenement.id = :evenementId " +
            "AND p.statut = :statut")
    int countByEvenementIdAndStatut(
            @Param("evenementId") Long evenementId,
            @Param("statut") String statut);

    //  Compte toutes les participations d'un événement
    int countByEvenementId(Long evenementId);

    //  Compte participations par organisateur et mois
    @Query("SELECT COUNT(p) FROM Participation p " +
            "WHERE p.evenement.organisateur.id = :organisateurId " +
            "AND MONTH(p.evenement.date) = :mois " +
            "AND YEAR(p.evenement.date) = :annee " +
            "AND p.statut = :statut")
    int countByOrganisateurAndMoisAndStatut(
            @Param("organisateurId") Long organisateurId,
            @Param("mois") int mois,
            @Param("annee") int annee,
            @Param("statut") String statut);

    //  Compte participations par candidat et statut
    int countByCandidatIdAndStatut(Long candidatId, String statut);

    //  Compte total participations par candidat
    int countByCandidatId(Long candidatId);

    //  Type d'événement le plus participé par le candidat
    @Query("SELECT p.evenement.type FROM Participation p " +
            "WHERE p.candidat.id = :candidatId " +
            "GROUP BY p.evenement.type " +
            "ORDER BY COUNT(p) DESC")
    List<String> findTypeFavoriByCandidat(@Param("candidatId") Long candidatId);



}
