package guesthouse.notification.dto.response;

import guesthouse.notification.domain.model.NotificationSetting;

public record NotificationSettingResponse(Boolean pushEnabled, Boolean chatPushEnabled) {

    public static NotificationSettingResponse from(NotificationSetting setting) {
        return new NotificationSettingResponse(setting.getPushEnabled(), setting.getChatPushEnabled());
    }
}
