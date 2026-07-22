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
@Table(name = "ai_conversation_message")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiConversationMessage extends TimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long conversationId;

    @Column(name = "message_role", nullable = false, length = 16)
    private String role;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 50)
    private String domain;

    private Double confidence;

    public AiConversationMessage(
            Long conversationId,
            String role,
            String content,
            String imageUrl,
            String domain,
            Double confidence
    ) {
        this.conversationId = conversationId;
        this.role = role;
        this.content = content;
        this.imageUrl = imageUrl;
        this.domain = domain;
        this.confidence = confidence;
    }

    public static AiConversationMessage user(
            Long conversationId,
            String content,
            String imageUrl
    ) {
        return new AiConversationMessage(
                conversationId,
                "user",
                content,
                imageUrl,
                null,
                null
        );
    }

    public static AiConversationMessage assistant(
            Long conversationId,
            String content,
            String domain,
            double confidence
    ) {
        return new AiConversationMessage(
                conversationId,
                "assistant",
                content,
                null,
                domain,
                confidence
        );
    }
}
