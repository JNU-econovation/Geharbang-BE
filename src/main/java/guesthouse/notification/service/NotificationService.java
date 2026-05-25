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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

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

    @Transactional
    public void createChatMessageNotification(Long receiverId, Long chatRoomId, String senderName, String content) {
        create(
                receiverId,
                NotificationType.CHAT_MESSAGE_CREATED,
                "새 채팅 메시지가 도착했습니다",
                createChatMessageContent(senderName, content),
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
    }
}
