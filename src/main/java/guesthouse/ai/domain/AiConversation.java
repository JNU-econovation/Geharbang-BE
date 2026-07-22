package guesthouse.ai.domain;

import guesthouse.common.domain.TimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "ai_conversation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiConversation extends TimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true, length = 100)
    private String sessionId;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(nullable = false, length = 300)
    private String lastMessage;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String contextJson;

    public AiConversation(
            Long userId,
            String sessionId,
            String title,
            String lastMessage,
            String contextJson
    ) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.title = title;
        this.lastMessage = lastMessage;
        this.contextJson = contextJson;
    }

    public void updateAfterAnswer(String lastMessage, String contextJson) {
        this.lastMessage = lastMessage;
        this.contextJson = contextJson;
    }
}
