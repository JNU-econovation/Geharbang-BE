package guesthouse.notification.dto.request;

public record NotificationSettingUpdateRequest(Boolean pushEnabled, Boolean chatPushEnabled) {}
