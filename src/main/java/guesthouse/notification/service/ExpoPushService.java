package guesthouse.notification.service;

import guesthouse.notification.domain.model.NotificationSetting;
import guesthouse.notification.domain.model.PushToken;
import guesthouse.notification.domain.vo.NotificationTargetType;
import guesthouse.notification.domain.vo.NotificationType;
import guesthouse.notification.dto.request.ExpoPushMessageRequest;
import guesthouse.notification.dto.response.ExpoPushResponse;
import guesthouse.notification.dto.response.ExpoPushTicket;
import guesthouse.notification.repository.PushTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpoPushService {

    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    private final PushTokenRepository pushTokenRepository;
    private final WebClient.Builder webClientBuilder;
    private final NotificationSettingService notificationSettingService;

    public void send(
            Long receiverId,
            String title,
            String content,
            NotificationType type,
            NotificationTargetType targetType,
            Long targetId
    ) {
        NotificationSetting setting = notificationSettingService.getOrCreate(receiverId);
        if (!setting.getPushEnabled()) {
            return;
        }

        List<PushToken> tokens = pushTokenRepository.findByUserIdAndIsActiveTrue(receiverId);
        if (tokens.isEmpty()) {
            return;
        }

        List<ExpoPushMessageRequest> messages = tokens.stream()
                .map(pushToken -> ExpoPushMessageRequest.of(
                        pushToken.getToken(),
                        title,
                        content,
                        type,
                        targetType,
                        targetId
                ))
                .toList();

        try {
            ExpoPushResponse response = webClientBuilder.build()
                    .post()
                    .uri(EXPO_PUSH_URL)
                    .bodyValue(messages)
                    .retrieve()
                    .bodyToMono(ExpoPushResponse.class)
                    .block(Duration.ofSeconds(5));

            if (response != null && response.data() != null) {
                deactivateInvalidTokens(tokens, response.data());
            }
        } catch (RuntimeException e) {
            log.warn("Expo push send failed. receiverId={}", receiverId, e);
        }
    }

    private void deactivateInvalidTokens(List<PushToken> tokens, List<ExpoPushTicket> tickets) {
        for (int i = 0; i < Math.min(tokens.size(), tickets.size()); i++) {
            if (tickets.get(i).isDeviceNotRegistered()) {
                tokens.get(i).deactivate();
                log.info("Deactivated invalid push token. token={}", tokens.get(i).getToken());
            }
        }
    }
}
