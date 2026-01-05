package guesthouse.guestHousePost.domain.dto;

import java.util.List;

public record GuestHousePostsResponse(
        List<GuestHousePostDto> guestHousePosts
) {

}
