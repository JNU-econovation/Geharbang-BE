package guesthouse.notification.repository;

import guesthouse.notification.domain.model.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PushTokenRepository extends JpaRepository<PushToken, Long> {

    Optional<PushToken> findByToken(String token);

    Optional<PushToken> findByTokenAndUserId(String token, Long userId);

    List<PushToken> findByUserIdAndIsActiveTrue(Long userId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update PushToken p set p.isActive = false where p.id in :ids")
    void deactivateByIds(@Param("ids") List<Long> ids);
}
