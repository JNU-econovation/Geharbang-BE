package guesthouse.notification.repository;

import guesthouse.notification.domain.model.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushTokenRepository extends JpaRepository<PushToken, Long> {

    Optional<PushToken> findByToken(String token);

    Optional<PushToken> findByTokenAndUserId(String token, Long userId);

    List<PushToken> findByUserIdAndIsActiveTrue(Long userId);
}
