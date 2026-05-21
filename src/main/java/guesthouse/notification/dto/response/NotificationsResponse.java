package guesthouse.notification.dto.response;

import java.util.List;

public record NotificationsResponse(
        List<NotificationDto> notifications
) {
}
