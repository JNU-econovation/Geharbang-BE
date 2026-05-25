package guesthouse.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PushTokenRegisterRequest(
        @NotBlank String token,
        @NotBlank @Pattern(regexp = "ios|android") String platform
) {
}
