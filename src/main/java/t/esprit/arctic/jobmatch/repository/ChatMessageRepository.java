package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import t.esprit.arctic.jobmatch.entity.ChatMessage;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {


    List<ChatMessage> findByEvenementIdOrderByEnvoyeAAsc(Long evenementId);
}