package guesthouse.notification.controller;

import guesthouse.common.annotation.UserId;
import guesthouse.notification.dto.request.PushTokenRegisterRequest;
import guesthouse.notification.service.PushTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Push Token", description = "푸시 토큰 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/push-tokens")
public class PushTokenController {

    private final PushTokenService pushTokenService;

    @Operation(summary = "푸시 토큰 등록", description = "로그인한 사용자의 Expo Push Token을 등록합니다.")
    @PostMapping
    public ResponseEntity<Void> register(
            @UserId Long userId,
            @Valid @RequestBody PushTokenRegisterRequest request
    ) {
        pushTokenService.register(userId, request.token(), request.platform());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "푸시 토큰 해제", description = "로그인한 사용자의 Expo Push Token을 비활성화합니다.")
    @DeleteMapping
    public ResponseEntity<Void> unregister(
            @UserId Long userId,
            @Valid @RequestBody PushTokenRegisterRequest request
    ) {
        pushTokenService.unregister(userId, request.token());
        return ResponseEntity.noContent().build();
    }
}
