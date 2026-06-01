package guesthouse.notification.service;

import guesthouse.application_record.domain.model.ApplicationRecord;
import guesthouse.certificate.domain.model.Certificate;
import guesthouse.notification.domain.model.Notification;
import guesthouse.notification.domain.vo.NotificationTargetType;
import guesthouse.notification.domain.vo.NotificationType;
import guesthouse.notification.dto.response.NotificationDto;
import guesthouse.notification.exception.NotificationErrorCode;
import guesthouse.notification.exception.NotificationException;
import guesthouse.notification.repository.NotificationRepository;
import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Duration CHAT_PUSH_THROTTLE_DURATION = Duration.ofSeconds(30);

    private final NotificationRepository notificationRepository;
    private final ExpoPushService expoPushService;
    private final NotificationSettingService notificationSettingService;
    private final Map<String, Instant> chatPushLastSentAt = new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    public List<NotificationDto> getNotifications(Long userId, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 10);

        return notificationRepository.findByReceiverIdOrderByIdDesc(userId, pageable).stream()
                .map(NotificationDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByReceiverIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndReceiverId(notificationId, userId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOT_FOUND));

        notification.markAsRead();
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByReceiverId(userId);
    }

    @Transactional
    public void createCertificateDecisionNotification(Certificate certificate, Boolean isApproved) {
        if (Boolean.TRUE.equals(isApproved)) {
            create(
                    certificate.getUserId(),
                    NotificationType.CERTIFICATE_APPROVED,
                    "사장님 인증이 승인되었습니다",
                    "이제 게스트하우스와 스태프 공고를 등록할 수 있어요.",
                    NotificationTargetType.CERTIFICATE,
                    certificate.getId()
            );
            return;
        }

        create(
                certificate.getUserId(),
                NotificationType.CERTIFICATE_REJECTED,
                "사장님 인증이 거절되었습니다",
                "제출한 인증 정보를 확인하고 다시 신청해주세요.",
                NotificationTargetType.CERTIFICATE,
                certificate.getId()
        );
    }

    @Transactional
    public void createStaffApplicationCreatedNotification(StaffRecruitment staffRecruitment, ApplicationRecord applicationRecord) {
        create(
                staffRecruitment.getOwnerId(),
                NotificationType.STAFF_APPLICATION_CREATED,
                "새 지원자가 도착했습니다",
                staffRecruitment.getTitle() + " 공고에 새 지원자가 있어요.",
                NotificationTargetType.APPLICATION_RECORD,
                applicationRecord.getId()
        );
    }

    @Transactional
    public void createApplicationAcceptedNotification(ApplicationRecord applicationRecord) {
        create(
                applicationRecord.getUserId(),
                NotificationType.APPLICATION_ACCEPTED,
                "지원 결과가 도착했습니다",
                getStaffRecruitmentTitle(applicationRecord) + " 공고에서 합격 처리되었습니다.",
                NotificationTargetType.APPLICATION_RECORD,
                applicationRecord.getId()
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createChatMessageNotification(Long receiverId, Long chatRoomId, String senderName, String content) {
        if (!notificationSettingService.getOrCreate(receiverId).getChatPushEnabled()) {
            return;
        }
        if (hasUnreadChatRoomNotification(receiverId, chatRoomId)) {
            return;
        }
        boolean shouldSendPush = shouldSendChatPush(receiverId, chatRoomId);
        create(
                receiverId,
                NotificationType.CHAT_MESSAGE_CREATED,
                "새 채팅 메시지가 도착했습니다",
                createChatMessageContent(senderName, content),
                NotificationTargetType.CHAT_ROOM,
                chatRoomId,
                shouldSendPush
        );
    }

    @Transactional
    public void markChatRoomNotificationsAsRead(Long receiverId, Long chatRoomId) {
        notificationRepository.markAsReadByReceiverIdAndTypeAndTarget(
                receiverId,
                NotificationType.CHAT_MESSAGE_CREATED,
                NotificationTargetType.CHAT_ROOM,
                chatRoomId
        );
    }

    private String getStaffRecruitmentTitle(ApplicationRecord applicationRecord) {
        if (applicationRecord.getStaffRecruitmentTitle() == null || applicationRecord.getStaffRecruitmentTitle().isBlank()) {
            return "지원한 스태프";
        }
        return applicationRecord.getStaffRecruitmentTitle();
    }

    private String createChatMessageContent(String senderName, String content) {
        String displayName = senderName == null || senderName.isBlank() ? "상대방" : senderName;
        String preview = content == null ? "" : content.strip();
        if (preview.length() > 40) {
            preview = preview.substring(0, 40) + "...";
        }
        return displayName + "님: " + preview;
    }

    private boolean hasUnreadChatRoomNotification(Long receiverId, Long chatRoomId) {
        return notificationRepository.existsByReceiverIdAndTypeAndTargetTypeAndTargetIdAndIsReadFalse(
                receiverId,
                NotificationType.CHAT_MESSAGE_CREATED,
                NotificationTargetType.CHAT_ROOM,
                chatRoomId
        );
    }

    private void create(
            Long receiverId,
            NotificationType type,
            String title,
            String content,
            NotificationTargetType targetType,
            Long targetId
    ) {
        Notification notification = Notification.builder()
                .receiverId(receiverId)
                .type(type)
                .title(title)
                .content(content)
                .targetType(targetType)
                .targetId(targetId)
                .build();

        notificationRepository.save(notification);
        runAfterCommitOrNow(() -> expoPushService.send(receiverId, title, content, type, targetType, targetId));
    }

    private void create(
            Long receiverId,
            NotificationType type,
            String title,
            String content,
            NotificationTargetType targetType,
            Long targetId,
            boolean shouldSendPush
    ) {
        Notification notification = Notification.builder()
                .receiverId(receiverId)
                .type(type)
                .title(title)
                .content(content)
                .targetType(targetType)
                .targetId(targetId)
                .build();

        notificationRepository.save(notification);
        if (shouldSendPush) {
            runAfterCommitOrNow(() -> expoPushService.send(receiverId, title, content, type, targetType, targetId));
        }
    }

    private boolean shouldSendChatPush(Long receiverId, Long chatRoomId) {
        String key = receiverId + ":" + chatRoomId;
        Instant now = Instant.now();
        AtomicBoolean throttled = new AtomicBoolean(false);

        chatPushLastSentAt.compute(key, (ignored, lastSentAt) -> {
            if (lastSentAt != null && Duration.between(lastSentAt, now).compareTo(CHAT_PUSH_THROTTLE_DURATION) < 0) {
                throttled.set(true);
                return lastSentAt;
            }
            return now;
        });

        return !throttled.get();
    }

    private void runAfterCommitOrNow(Runnable task) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            task.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                task.run();
            }
        });
    }
}
