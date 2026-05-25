package guesthouse.notification.service;

import guesthouse.notification.domain.model.NotificationSetting;
import guesthouse.notification.dto.request.NotificationSettingUpdateRequest;
import guesthouse.notification.dto.response.NotificationSettingResponse;
import guesthouse.notification.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationSettingService {

    private final NotificationSettingRepository notificationSettingRepository;

    @Transactional
    public NotificationSetting getOrCreate(Long userId) {
        return notificationSettingRepository.findByUserId(userId)
                .orElseGet(() -> notificationSettingRepository.save(new NotificationSetting(userId)));
    }

    @Transactional
    public NotificationSettingResponse getSetting(Long userId) {
        return NotificationSettingResponse.from(getOrCreate(userId));
    }

    @Transactional
    public NotificationSettingResponse updateSetting(Long userId, NotificationSettingUpdateRequest request) {
        NotificationSetting setting = getOrCreate(userId);
        setting.update(request.pushEnabled(), request.chatPushEnabled());
        return NotificationSettingResponse.from(setting);
    }
}
