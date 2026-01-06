package guesthouse.guestHousePost.dto;

import guesthouse.guestHousePost.domain.model.GuestHousePost;
import lombok.Builder;

import java.util.List;

@Builder
public record RandomGuestHousePostDto(
        Long id,
        String guestHouseName,
        List<String> tags,
        String imageUrl
) {

    public static RandomGuestHousePostDto of(GuestHousePost guestHousePost, String imageUrl) {
        return new RandomGuestHousePostDto(
                guestHousePost.getId(),
                guestHousePost.getGuestHouseName(),
                guestHousePost.getMoods().stream().map(Enum::name).toList(),
                imageUrl
        );
    }

}
