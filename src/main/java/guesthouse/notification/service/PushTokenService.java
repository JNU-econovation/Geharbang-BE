package guesthouse.notification.service;

import guesthouse.notification.domain.model.PushToken;
import guesthouse.notification.repository.PushTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PushTokenService {

    private final PushTokenRepository pushTokenRepository;

    @Transactional
    public void register(Long userId, String token, String platform) {
        pushTokenRepository.findByToken(token)
                .ifPresentOrElse(
                        pushToken -> pushToken.activate(userId, platform),
                        () -> pushTokenRepository.save(new PushToken(userId, token, platform))
                );
    }

    @Transactional
    public void unregister(Long userId, String token) {
        pushTokenRepository.findByTokenAndUserId(token, userId)
                .ifPresent(PushToken::deactivate);
    }
}
