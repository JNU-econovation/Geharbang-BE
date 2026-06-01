package guesthouse.notification.service;

import guesthouse.notification.domain.model.NotificationSetting;
import guesthouse.notification.repository.NotificationSettingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationSettingServiceTest {

    @Mock
    private NotificationSettingRepository notificationSettingRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    private NotificationSettingService notificationSettingService;

    @BeforeEach
    void setUp() {
        notificationSettingService = new NotificationSettingService(
                notificationSettingRepository,
                transactionManager
        );
    }

    @Test
    void getOrCreate_returnsExistingSetting() {
        NotificationSetting setting = new NotificationSetting(1L);
        when(notificationSettingRepository.findByUserId(1L)).thenReturn(Optional.of(setting));

        NotificationSetting result = notificationSettingService.getOrCreate(1L);

        assertThat(result).isSameAs(setting);
    }

    @Test
    void getOrCreate_refetchesWhenConcurrentInsertAlreadyCreatedSetting() {
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        NotificationSetting setting = new NotificationSetting(1L);
        when(notificationSettingRepository.findByUserId(1L))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(setting));
        when(notificationSettingRepository.saveAndFlush(any(NotificationSetting.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate userId"));

        NotificationSetting result = notificationSettingService.getOrCreate(1L);

        assertThat(result).isSameAs(setting);
        verify(transactionManager).rollback(transactionStatus);
    }
}
