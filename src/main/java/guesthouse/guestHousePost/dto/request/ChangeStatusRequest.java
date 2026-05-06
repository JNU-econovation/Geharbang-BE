package guesthouse.guestHousePost.dto.request;

import guesthouse.guestHousePost.domain.vo.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "GuestHouseChangeStatusRequest")
public record ChangeStatusRequest(
        @NotNull
        @Schema(description = "변경할 게시글 상태", example = "ACTIVE")
        Status status
) {
}
