package guesthouse.wish.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "찜 응답")
public record WishResponse(
        @Schema(description = "찜 ID", example = "1")
        Long wishId
) {

}
