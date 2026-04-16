package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import t.esprit.arctic.jobmatch.entity.Evenement;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EvenementRepository extends JpaRepository<Evenement, Long> {
    List<Evenement> findByType(String type);
    List<Evenement> findByLieu(String lieu);
    List<Evenement> findByOrganisateurId(Long organisateurId);

<<<<<<< HEAD
    // Compte les événements d'un organisateur par mois et année
    @Query("SELECT COUNT(e) FROM Evenement e " +
            "WHERE MONTH(e.date) = :mois " +
            "AND YEAR(e.date) = :annee " +
=======
    @Query("SELECT COUNT(e) FROM Evenement e " +
            "WHERE MONTH(e.dateHeure) = :mois " +
            "AND YEAR(e.dateHeure) = :annee " +
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
            "AND e.organisateur.id = :organisateurId")
    int countByMoisAndAnneeAndOrganisateur(
            @Param("mois") int mois,
            @Param("annee") int annee,
            @Param("organisateurId") Long organisateurId);

<<<<<<< HEAD
    // Récupère les événements d'un organisateur par mois
    @Query("SELECT e FROM Evenement e " +
            "WHERE MONTH(e.date) = :mois " +
            "AND YEAR(e.date) = :annee " +
=======
    @Query("SELECT e FROM Evenement e " +
            "WHERE MONTH(e.dateHeure) = :mois " +
            "AND YEAR(e.dateHeure) = :annee " +
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
            "AND e.organisateur.id = :organisateurId")
    List<Evenement> findByMoisAndAnneeAndOrganisateur(
            @Param("mois") int mois,
            @Param("annee") int annee,
            @Param("organisateurId") Long organisateurId);
}