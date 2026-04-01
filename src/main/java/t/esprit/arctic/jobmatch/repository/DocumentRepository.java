package t.esprit.arctic.jobmatch.repository;

import t.esprit.arctic.jobmatch.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}