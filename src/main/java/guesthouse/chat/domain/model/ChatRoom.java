package guesthouse.chat.domain.model;

import guesthouse.common.domain.TimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "chat_room")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends TimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long applicationRecordId;

    @Column(nullable = false)
    private Long staffRecruitmentId;

    @Column(nullable = false)
    private String staffRecruitmentTitle;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private Long applicantId;

    private String lastMessage;

    private LocalDateTime lastMessageAt;

    public ChatRoom(
            Long applicationRecordId,
            Long staffRecruitmentId,
            String staffRecruitmentTitle,
            Long ownerId,
            Long applicantId
    ) {
        this.applicationRecordId = applicationRecordId;
        this.staffRecruitmentId = staffRecruitmentId;
        this.staffRecruitmentTitle = staffRecruitmentTitle;
        this.ownerId = ownerId;
        this.applicantId = applicantId;
    }

    public boolean isParticipant(Long userId) {
        return ownerId.equals(userId) || applicantId.equals(userId);
    }

    public Long getOpponentId(Long userId) {
        if (ownerId.equals(userId)) {
            return applicantId;
        }
        return ownerId;
    }

    public void updateLastMessage(String lastMessage, LocalDateTime lastMessageAt) {
        this.lastMessage = lastMessage;
        this.lastMessageAt = lastMessageAt;
    }
}
