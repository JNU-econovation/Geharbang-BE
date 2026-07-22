package guesthouse.ai.repository;

import guesthouse.ai.domain.AiConversationMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiConversationMessageRepository extends JpaRepository<AiConversationMessage, Long> {

    List<AiConversationMessage> findAllByConversationIdOrderByIdAsc(Long conversationId);

    void deleteAllByConversationId(Long conversationId);
}
