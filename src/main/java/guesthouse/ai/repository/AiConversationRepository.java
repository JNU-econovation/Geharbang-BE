package guesthouse.ai.repository;

import guesthouse.ai.domain.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {

    List<AiConversation> findAllByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<AiConversation> findBySessionIdAndUserId(String sessionId, Long userId);
}
