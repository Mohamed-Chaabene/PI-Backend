package t.esprit.arctic.jobmatch.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import t.esprit.arctic.jobmatch.entity.OffrePartenaire;
import t.esprit.arctic.jobmatch.entity.TypeOffrePartenaire;

public interface OffrePartenaireRepository extends JpaRepository<OffrePartenaire, Long>  {
    List<OffrePartenaire> findByPartenaireId(Long partenaireId);
    List<OffrePartenaire> findByType(TypeOffrePartenaire type);
}
