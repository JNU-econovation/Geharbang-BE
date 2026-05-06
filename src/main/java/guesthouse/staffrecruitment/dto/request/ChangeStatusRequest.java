package guesthouse.staffrecruitment.dto.request;

import guesthouse.staffrecruitment.domain.vo.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ChangeStatusRequest(
        @NotNull
        @Schema(description = "변경할 공고 상태", example = "ACTIVE")
        Status status
) {
}
