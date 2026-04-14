package t.esprit.arctic.jobmatch.repository;

import  org.springframework.data.jpa.repository.JpaRepository;
import t.esprit.arctic.jobmatch.entity.Partenaire;
import org.springframework.data.jpa.repository.Query;
import java.util.List;


import t.esprit.arctic.jobmatch.entity.TypePartenaire;

public interface PartenaireRepository extends JpaRepository<Partenaire, Long>{
    List<Partenaire> findByType(TypePartenaire type);
    @Query("SELECT p FROM Partenaire p LEFT JOIN p.offres o GROUP BY p ORDER BY COUNT(o) DESC")
    List<Partenaire> findTopByOffres();
}
