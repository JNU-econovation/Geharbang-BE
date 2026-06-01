package guesthouse.notification.repository;

import guesthouse.notification.domain.model.Notification;
import guesthouse.notification.domain.vo.NotificationTargetType;
import guesthouse.notification.domain.vo.NotificationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByReceiverIdOrderByIdDesc(Long receiverId, Pageable pageable);

    long countByReceiverIdAndIsReadFalse(Long receiverId);

    Optional<Notification> findByIdAndReceiverId(Long id, Long receiverId);

    boolean existsByReceiverIdAndTypeAndTargetTypeAndTargetIdAndIsReadFalse(
            Long receiverId,
            NotificationType type,
            NotificationTargetType targetType,
            Long targetId
    );

    @Modifying(clearAutomatically = true)
    @Query("update Notification n set n.isRead = true where n.receiverId = :receiverId and n.isRead = false")
    void markAllAsReadByReceiverId(@Param("receiverId") Long receiverId);
}
