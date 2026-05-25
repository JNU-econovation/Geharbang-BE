package guesthouse.notification.dto.request;

import guesthouse.notification.domain.vo.NotificationTargetType;
import guesthouse.notification.domain.vo.NotificationType;

import java.util.HashMap;
import java.util.Map;

public record ExpoPushMessageRequest(
        String to,
        String title,
        String body,
        Map<String, Object> data
) {
    public static ExpoPushMessageRequest of(
            String token,
            String title,
            String body,
            NotificationType type,
            NotificationTargetType targetType,
            Long targetId
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", type.name());
        data.put("targetType", targetType.name());
        if (targetId != null) {
            data.put("targetId", targetId);
        }

        return new ExpoPushMessageRequest(
                token,
                title,
                body,
                data
        );
    }
}
