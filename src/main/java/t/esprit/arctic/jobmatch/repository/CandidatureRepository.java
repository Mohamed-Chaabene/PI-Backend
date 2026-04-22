package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import t.esprit.arctic.jobmatch.entity.Candidature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface CandidatureRepository extends JpaRepository<Candidature, Long> {

    // ==================== MÉTHODES DE BASE ====================

    List<Candidature> findByCandidatId(Long candidatId);

    // Trouver les candidatures d'une offre
    List<Candidature> findByOffreEmploiId(Long offreId);

    // Trouver les candidatures par statut
    List<Candidature> findByStatut(String statut);

    List<Candidature> findAllByOrderByDateEnvoiDesc();

    List<Candidature> findByCandidatIdAndStatut(Long candidatId, String statut);

    Optional<Candidature> findTopByCandidatIdAndOffreEmploiIdOrderByDateEnvoiDesc(Long candidatId, Long offreId);

    // Trouve les candidatures en attente depuis avant une certaine date
    List<Candidature> findByStatutAndDateEnvoiBefore(String statut, LocalDateTime date);

    // Trouve les candidatures envoyées avant une certaine date
    List<Candidature> findByDateEnvoiBefore(LocalDateTime date);

    // Trouve les candidatures par offre et statut
    List<Candidature> findByOffreEmploiIdAndStatut(Long offreId, String statut);

    // Compte les candidatures par statut après une certaine date
    long countByStatutAndDateEnvoiAfter(String statut, LocalDateTime date);

    // Compte les candidatures par statut entre deux dates
    long countByStatutAndDateEnvoiBetween(String statut, LocalDateTime debut, LocalDateTime fin);

    // Compte les candidatures par candidat, statut et avant une date
    long countByCandidatIdAndStatutAndDateEnvoiBefore(Long candidatId, String statut, LocalDateTime date);

    // Compte les candidatures entre deux dates pour un candidat
    long countByCandidatIdAndDateEnvoiBetween(Long candidatId, LocalDateTime debut, LocalDateTime fin);

    // Trouve les candidatures à relancer (en attente depuis plus de X jours)
    List<Candidature> findByCandidatIdAndStatutAndDateEnvoiBefore(Long candidatId, String statut, LocalDateTime date);

    // Trouve les candidatures sans réponse (en attente, avant date, nbRelances < seuil)
    List<Candidature> findByCandidatIdAndStatutAndDateEnvoiBeforeAndNbRelancesLessThan(Long candidatId, String statut, LocalDateTime date, Integer nbRelances);

    // Compte les candidatures par email pour détecter les doublons
    List<Candidature> findByCandidatIdAndEmail(Long candidatId, String email);

    // Trouve les candidatures archivées
    List<Candidature> findByArchiveTrue();

    // Trouve les candidatures non archivées
    List<Candidature> findByArchiveFalseOrArchiveIsNull();

    // Trouve les candidatures qui nécessitent une attention particulière
    List<Candidature> findByCandidatIdAndNecessiteAttentionTrue(Long candidatId);

    // Archive les anciennes candidatures (update via @Modifying)
    List<Candidature> findByDateEnvoiBeforeAndArchiveFalse(LocalDateTime date);

    // ==================== MÉTHODES POUR L'ARCHIVAGE (CORRIGÉES) ====================

    /**
     * Trouver les candidatures de plus de 7 jours non archivées
     */
    @Query("SELECT c FROM Candidature c WHERE c.dateEnvoi < :dateLimite AND (c.archive = false OR c.archive IS NULL)")
    List<Candidature> findCandidaturesPlusDe7Jours(@Param("dateLimite") Date dateLimite);

    /**
     * Archiver les candidatures de plus de 7 jours
     */
    @Modifying
    @Transactional
    @Query("UPDATE Candidature c SET c.archive = true, c.archiveDate = :dateArchive WHERE c.dateEnvoi < :dateLimite AND (c.archive = false OR c.archive IS NULL)")
    int archiverCandidaturesPlusDe7Jours(@Param("dateLimite") Date dateLimite,
                                         @Param("dateArchive") LocalDateTime dateArchive);

    /**
     * Restaurer une candidature archivée
     */
    @Modifying
    @Transactional
    @Query("UPDATE Candidature c SET c.archive = false, c.archiveDate = null WHERE c.id = :id")
    int restaurerCandidature(@Param("id") Long id);

    // ==================== MÉTHODES JPQL AVEC JOIN ====================

    /**
     * 1. Jointure entre Candidature et Candidat
     * Récupère les candidatures avec les infos du candidat
     */
    @Query("SELECT c, ca FROM Candidature c JOIN c.candidat ca WHERE ca.nom = :nom")
    List<Object[]> findCandidaturesByCandidatNom(@Param("nom") String nom);

    /**
     * 2. Jointure entre Candidature et OffreEmploi
     * Récupère les candidatures avec les infos de l'offre
     */
    @Query("SELECT c, o FROM Candidature c JOIN c.offreEmploi o WHERE o.entreprise = :entreprise")
    List<Object[]> findCandidaturesByOffreEntreprise(@Param("entreprise") String entreprise);

    /**
     * 3. Jointure entre Candidature et Document (via OneToOne)
     * Récupère les candidatures qui ont un document (CV)
     */
    @Query("SELECT c, d FROM Candidature c JOIN c.document d WHERE d.type = :type")
    List<Object[]> findCandidaturesByDocumentType(@Param("type") String type);

    /**
     * 4. Jointure complète: Candidature + Candidat + OffreEmploi
     * Récupère toutes les infos pour un statut donné
     */
    @Query("SELECT c, ca, o FROM Candidature c " +
            "JOIN c.candidat ca " +
            "JOIN c.offreEmploi o " +
            "WHERE c.statut = :statut")
    List<Object[]> findFullCandidaturesByStatut(@Param("statut") String statut);

    /**
     * 5. LEFT JOIN pour inclure les candidatures sans document
     * Récupère toutes les candidatures avec ou sans document
     */
    @Query("SELECT c, ca, d FROM Candidature c " +
            "JOIN c.candidat ca " +
            "LEFT JOIN c.document d")
    List<Object[]> findAllCandidaturesWithLeftJoinDocument();

    /**
     * 6. Jointure avec conditions multiples
     * Récupère les candidatures par entreprise et statut
     */
    @Query("SELECT c, ca, o FROM Candidature c " +
            "JOIN c.candidat ca " +
            "JOIN c.offreEmploi o " +
            "WHERE o.entreprise = :entreprise AND c.statut = :statut")
    List<Object[]> findCandidaturesByEntrepriseAndStatut(
            @Param("entreprise") String entreprise,
            @Param("statut") String statut);

    /**
     * 7. Statistiques par candidat (avec COUNT)
     * Récupère le nombre de candidatures et d'acceptations par candidat
     */
    @Query("SELECT ca.id, ca.nom, ca.prenom, COUNT(c), " +
            "SUM(CASE WHEN c.statut = 'ACCEPTEE' THEN 1 ELSE 0 END) " +
            "FROM Candidature c JOIN c.candidat ca " +
            "GROUP BY ca.id, ca.nom, ca.prenom " +
            "ORDER BY COUNT(c) DESC")
    List<Object[]> getStatsByCandidat();

    /**
     * 8. Candidatures avec salaire minimum
     * Récupère les candidatures pour les offres bien payées
     */
    @Query("SELECT c, ca, o FROM Candidature c " +
            "JOIN c.candidat ca " +
            "JOIN c.offreEmploi o " +
            "WHERE o.salary >= :minSalary")
    List<Object[]> findCandidaturesByMinSalary(@Param("minSalary") Double minSalary);
}