package guesthouse.notification.controller;

import guesthouse.common.annotation.UserId;
import guesthouse.notification.dto.response.NotificationsResponse;
import guesthouse.notification.dto.response.UnreadNotificationCountResponse;
import guesthouse.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notification", description = "알림 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "내 알림 목록 조회", description = "로그인한 사용자의 알림 목록을 최신순으로 조회합니다.")
    @GetMapping
    public ResponseEntity<NotificationsResponse> getNotifications(
            @UserId Long userId,
            @Parameter(description = "페이지 번호. 0부터 시작", example = "0")
            @RequestParam(defaultValue = "0") int pageNumber
    ) {
        return ResponseEntity.ok(new NotificationsResponse(notificationService.getNotifications(userId, pageNumber)));
    }

    @Operation(summary = "읽지 않은 알림 개수 조회", description = "로그인한 사용자의 읽지 않은 알림 개수를 조회합니다.")
    @GetMapping("/unread-count")
    public ResponseEntity<UnreadNotificationCountResponse> getUnreadCount(@UserId Long userId) {
        return ResponseEntity.ok(new UnreadNotificationCountResponse(notificationService.getUnreadCount(userId)));
    }

    @Operation(summary = "알림 읽음 처리", description = "로그인한 사용자의 특정 알림을 읽음 처리합니다.")
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @UserId Long userId,
            @Parameter(description = "알림 ID", example = "1") @PathVariable Long id
    ) {
        notificationService.markAsRead(userId, id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "전체 알림 읽음 처리", description = "로그인한 사용자의 모든 읽지 않은 알림을 읽음 처리합니다.")
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@UserId Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
}
