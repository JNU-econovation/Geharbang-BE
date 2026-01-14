package guesthouse.guestHousePost.dto.response;

import java.util.List;

public record OwnerGuestHousePostsResponse(
        List<OwnerGuestHousePostDto> guestHousePosts
) {
}
