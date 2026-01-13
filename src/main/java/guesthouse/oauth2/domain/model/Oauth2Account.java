package guesthouse.oauth2.domain.model;

import guesthouse.common.domain.TimeEntity;
import guesthouse.oauth2.domain.vo.Provider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Oauth2Account extends TimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(nullable = false)
    private String socialId;

    @Builder
    public Oauth2Account(Long userId, Provider provider, String socialId) {
        this.userId = userId;
        this.provider = provider;
        this.socialId = socialId;
    }
}
