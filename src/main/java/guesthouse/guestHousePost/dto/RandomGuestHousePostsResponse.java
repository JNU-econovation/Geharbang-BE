package guesthouse.guestHousePost.dto;

import java.util.List;

public record RandomGuestHousePostsResponse(
        List<RandomGuestHousePostDto> guestHouseRecommendation
) { }
