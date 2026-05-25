package guesthouse.notification.service;

import guesthouse.application_record.domain.model.ApplicationRecord;
import guesthouse.certificate.domain.model.Certificate;
import guesthouse.certificate.domain.vo.CertificateType;
import guesthouse.notification.domain.model.Notification;
import guesthouse.notification.domain.model.NotificationSetting;
import guesthouse.notification.domain.vo.NotificationTargetType;
import guesthouse.notification.domain.vo.NotificationType;
import guesthouse.notification.exception.NotificationException;
import guesthouse.notification.repository.NotificationRepository;
import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import guesthouse.staffrecruitment.domain.vo.Region;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ExpoPushService expoPushService;

    @Mock
    private NotificationSettingService notificationSettingService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void createCertificateDecisionNotification_savesApprovedNotification() {
        Certificate certificate = createCertificate(1L);

        notificationService.createCertificateDecisionNotification(certificate, true);

        Notification notification = captureSavedNotification();
        assertThat(notification.getReceiverId()).isEqualTo(1L);
        assertThat(notification.getType()).isEqualTo(NotificationType.CERTIFICATE_APPROVED);
        assertThat(notification.getTargetType()).isEqualTo(NotificationTargetType.CERTIFICATE);
    }

    @Test
    void createCertificateDecisionNotification_savesRejectedNotification() {
        Certificate certificate = createCertificate(1L);

        notificationService.createCertificateDecisionNotification(certificate, false);

        Notification notification = captureSavedNotification();
        assertThat(notification.getReceiverId()).isEqualTo(1L);
        assertThat(notification.getType()).isEqualTo(NotificationType.CERTIFICATE_REJECTED);
        assertThat(notification.getTargetType()).isEqualTo(NotificationTargetType.CERTIFICATE);
    }

    @Test
    void createStaffApplicationCreatedNotification_savesNotificationToOwner() {
        StaffRecruitment staffRecruitment = createStaffRecruitment(10L, 2L, "제주 스텝 모집");
        ApplicationRecord applicationRecord = createApplicationRecord(10L, 3L, "제주 스텝 모집");

        notificationService.createStaffApplicationCreatedNotification(staffRecruitment, applicationRecord);

        Notification notification = captureSavedNotification();
        assertThat(notification.getReceiverId()).isEqualTo(2L);
        assertThat(notification.getType()).isEqualTo(NotificationType.STAFF_APPLICATION_CREATED);
        assertThat(notification.getTargetType()).isEqualTo(NotificationTargetType.APPLICATION_RECORD);
    }

    @Test
    void createApplicationAcceptedNotification_savesNotificationToApplicant() {
        ApplicationRecord applicationRecord = createApplicationRecord(10L, 3L, "제주 스텝 모집");

        notificationService.createApplicationAcceptedNotification(applicationRecord);

        Notification notification = captureSavedNotification();
        assertThat(notification.getReceiverId()).isEqualTo(3L);
        assertThat(notification.getType()).isEqualTo(NotificationType.APPLICATION_ACCEPTED);
        assertThat(notification.getContent()).contains("제주 스텝 모집");
    }

    @Test
    void createChatMessageNotification_savesWhenChatNotificationEnabled() {
        when(notificationSettingService.getOrCreate(3L)).thenReturn(new NotificationSetting(3L));

        notificationService.createChatMessageNotification(3L, 10L, "망치", "안녕하세요");

        Notification notification = captureSavedNotification();
        assertThat(notification.getReceiverId()).isEqualTo(3L);
        assertThat(notification.getType()).isEqualTo(NotificationType.CHAT_MESSAGE_CREATED);
        assertThat(notification.getTargetType()).isEqualTo(NotificationTargetType.CHAT_ROOM);
        assertThat(notification.getTargetId()).isEqualTo(10L);
        assertThat(notification.getContent()).contains("망치님");
        verify(expoPushService).send(
                eq(3L),
                eq("새 채팅 메시지가 도착했습니다"),
                anyString(),
                eq(NotificationType.CHAT_MESSAGE_CREATED),
                eq(NotificationTargetType.CHAT_ROOM),
                eq(10L)
        );
    }

    @Test
    void createChatMessageNotification_skipsWhenChatNotificationDisabled() {
        NotificationSetting setting = new NotificationSetting(3L);
        setting.update(null, false);
        when(notificationSettingService.getOrCreate(3L)).thenReturn(setting);

        notificationService.createChatMessageNotification(3L, 10L, "망치", "안녕하세요");

        verify(notificationRepository, never()).save(any());
        verify(expoPushService, never()).send(anyLong(), anyString(), anyString(), any(), any(), any());
    }

    @Test
    void markAsRead_marksOwnedNotification() {
        Notification notification = Notification.builder()
                .receiverId(1L)
                .type(NotificationType.APPLICATION_ACCEPTED)
                .title("title")
                .content("content")
                .targetType(NotificationTargetType.APPLICATION_RECORD)
                .targetId(10L)
                .build();
        when(notificationRepository.findByIdAndReceiverId(100L, 1L)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(1L, 100L);

        assertThat(notification.getIsRead()).isTrue();
    }

    @Test
    void markAsRead_throwsWhenNotificationIsNotOwned() {
        when(notificationRepository.findByIdAndReceiverId(100L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(1L, 100L))
                .isInstanceOf(NotificationException.class);
    }

    @Test
    void markAllAsRead_usesBulkUpdate() {
        notificationService.markAllAsRead(1L);

        verify(notificationRepository).markAllAsReadByReceiverId(1L);
        verify(notificationRepository, never()).findByIdAndReceiverId(anyLong(), anyLong());
    }

    private Notification captureSavedNotification() {
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        return captor.getValue();
    }

    private Certificate createCertificate(Long userId) {
        return Certificate.builder()
                .certificateType(CertificateType.영업신고증)
                .guestHouseName("제주 게스트하우스")
                .ownerName("사장님")
                .phoneNumber("010-0000-0000")
                .fileUrl("/files/certifications/12345678test.pdf")
                .userId(userId)
                .build();
    }

    private StaffRecruitment createStaffRecruitment(Long id, Long ownerId, String title) {
        return StaffRecruitment.builder()
                .id(id)
                .ownerId(ownerId)
                .title(title)
                .guestHouseName("제주 게스트하우스")
                .region(Region.제주시)
                .build();
    }

    private ApplicationRecord createApplicationRecord(Long recruitmentId, Long userId, String title) {
        return new ApplicationRecord(
                recruitmentId,
                userId,
                "{}",
                title,
                Region.제주시,
                "/images/test.png"
        );
    }
}
