package guesthouse.notification.domain.model;

import guesthouse.common.domain.TimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting extends TimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private Boolean pushEnabled;

    @Column(nullable = false)
    private Boolean chatPushEnabled;

    public NotificationSetting(Long userId) {
        this.userId = userId;
        this.pushEnabled = true;
        this.chatPushEnabled = true;
    }

    public void update(Boolean pushEnabled, Boolean chatPushEnabled) {
        if (pushEnabled != null) {
            this.pushEnabled = pushEnabled;
        }
        if (chatPushEnabled != null) {
            this.chatPushEnabled = chatPushEnabled;
        }
    }
}
