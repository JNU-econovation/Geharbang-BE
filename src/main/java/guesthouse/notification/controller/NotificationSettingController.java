package guesthouse.notification.controller;

import guesthouse.common.annotation.UserId;
import guesthouse.notification.dto.request.NotificationSettingUpdateRequest;
import guesthouse.notification.dto.response.NotificationSettingResponse;
import guesthouse.notification.service.NotificationSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "NotificationSetting", description = "알림 설정 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notification-settings")
public class NotificationSettingController {

    private final NotificationSettingService notificationSettingService;

    @Operation(summary = "알림 설정 조회")
    @GetMapping
    public ResponseEntity<NotificationSettingResponse> getSetting(@UserId Long userId) {
        return ResponseEntity.ok(notificationSettingService.getSetting(userId));
    }

    @Operation(summary = "알림 설정 변경")
    @PatchMapping
    public ResponseEntity<NotificationSettingResponse> updateSetting(
            @UserId Long userId,
            @RequestBody NotificationSettingUpdateRequest request
    ) {
        return ResponseEntity.ok(notificationSettingService.updateSetting(userId, request));
    }
}
