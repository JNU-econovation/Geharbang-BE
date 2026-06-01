package guesthouse.notification.service;

import guesthouse.notification.domain.model.NotificationSetting;
import guesthouse.notification.dto.request.NotificationSettingUpdateRequest;
import guesthouse.notification.dto.response.NotificationSettingResponse;
import guesthouse.notification.repository.NotificationSettingRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class NotificationSettingService {

    private final NotificationSettingRepository notificationSettingRepository;
    private final TransactionTemplate requiresNewTemplate;

    public NotificationSettingService(
            NotificationSettingRepository notificationSettingRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.notificationSettingRepository = notificationSettingRepository;
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.requiresNewTemplate = template;
    }

    @Transactional
    public NotificationSetting getOrCreate(Long userId) {
        return notificationSettingRepository.findByUserId(userId)
                .orElseGet(() -> {
                    try {
                        // 별도 트랜잭션으로 INSERT 시도.
                        // 실패 시 그 트랜잭션만 롤백되어 JDBC 커넥션이 깨끗이 반환된다.
                        return requiresNewTemplate.execute(status ->
                                notificationSettingRepository.saveAndFlush(new NotificationSetting(userId)));
                    } catch (DataIntegrityViolationException e) {
                        // 동시에 다른 스레드가 먼저 INSERT한 경우 → 그 행을 읽어 반환
                        return notificationSettingRepository.findByUserId(userId).orElseThrow();
                    }
                });
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
