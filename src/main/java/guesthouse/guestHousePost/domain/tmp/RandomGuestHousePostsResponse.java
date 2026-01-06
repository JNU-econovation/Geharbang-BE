package guesthouse.guestHousePost.domain.tmp;

import java.util.List;

public record RandomGuestHousePostsResponse(
        List<RandomGuestHousePostDto> guestHouseRecommendation
) { }
