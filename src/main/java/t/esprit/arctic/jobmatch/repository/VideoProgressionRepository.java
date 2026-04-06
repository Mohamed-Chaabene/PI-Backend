package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import t.esprit.arctic.jobmatch.entity.VideoProgression;
import java.util.List;
import java.util.Optional;

public interface VideoProgressionRepository
        extends JpaRepository<VideoProgression, Long> {

    List<VideoProgression> findByInscriptionId(Long inscriptionId);

    Optional<VideoProgression> findByInscriptionIdAndVideoId(
            Long inscriptionId, String videoId);

    long countByInscriptionIdAndVuCompleteTrue(Long inscriptionId);
    long countByInscriptionIdAndQuizReussiTrue(Long inscriptionId);
}