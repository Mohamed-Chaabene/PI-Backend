package t.esprit.arctic.jobmatch.repository;

import  org.springframework.data.jpa.repository.JpaRepository;
import t.esprit.arctic.jobmatch.entity.Partenaire;

import java.util.List;


import t.esprit.arctic.jobmatch.entity.TypePartenaire;

public interface PartenaireRepository extends JpaRepository<Partenaire, Long>{
    List<Partenaire> findByType(TypePartenaire type);
}
