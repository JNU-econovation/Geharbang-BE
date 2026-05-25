package guesthouse.notification.domain.model;

import guesthouse.common.domain.TimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushToken extends TimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(nullable = false, length = 20)
    private String platform;

    @Column(nullable = false)
    private Boolean isActive;

    public PushToken(Long userId, String token, String platform) {
        this.userId = userId;
        this.token = token;
        this.platform = platform;
        this.isActive = true;
    }

    public void activate(Long userId, String platform) {
        this.userId = userId;
        this.platform = platform;
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }
}
