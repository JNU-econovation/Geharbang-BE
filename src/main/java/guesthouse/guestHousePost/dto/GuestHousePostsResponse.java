package guesthouse.guestHousePost.dto;

import java.util.List;

public record GuestHousePostsResponse(
        List<GuestHousePostDto> guestHousePosts
) {

}
