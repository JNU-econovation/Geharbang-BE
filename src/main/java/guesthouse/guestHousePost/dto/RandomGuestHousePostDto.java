package guesthouse.guestHousePost.dto;

import guesthouse.guestHousePost.domain.model.GuestHousePost;
import lombok.Builder;

import java.util.List;

@Builder
public record RandomGuestHousePostDto(
        Long id,
        String guestHouseName,
        String address,
        List<String> tags,
        String imageUrl
) {

    public static RandomGuestHousePostDto of(GuestHousePost guestHousePost, String imageUrl) {
        return new RandomGuestHousePostDto(
                guestHousePost.getId(),
                guestHousePost.getGuestHouseName(),
                getAddress(guestHousePost.getRoadNameAddress(), guestHousePost.getLotNumberAddress()),
                guestHousePost.getMoods().stream().map(Enum::name).toList(),
                imageUrl
        );
    }

    private static String getAddress(String roadNameAddress, String lotNumberAddress) {
        if (roadNameAddress != null && !roadNameAddress.isBlank()) {
            return roadNameAddress;
        }
        return lotNumberAddress;
    }

}
