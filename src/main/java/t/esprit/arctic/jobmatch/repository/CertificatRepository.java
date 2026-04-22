package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import t.esprit.arctic.jobmatch.entity.Certificat;
import java.util.List;
import java.util.Optional;

public interface CertificatRepository extends JpaRepository<Certificat, Long> {
    List<Certificat> findByInscriptionCandidatId(Long candidatId);
    Optional<Certificat> findByInscriptionId(Long inscriptionId);
    boolean existsByInscriptionId(Long inscriptionId);
}