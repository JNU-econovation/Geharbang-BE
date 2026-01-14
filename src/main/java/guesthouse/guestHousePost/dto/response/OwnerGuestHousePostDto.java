package guesthouse.guestHousePost.dto.response;

import guesthouse.guestHousePost.domain.model.GuestHousePost;

import java.util.ArrayList;
import java.util.List;

public record OwnerGuestHousePostDto(
        Long id,
        String guestHouseName,
        String roadNameAddress,
        String imageUrl,
        boolean isClosed
) {

    public static OwnerGuestHousePostDto of(GuestHousePost guestHousePost, String imageUrl) {
        return new OwnerGuestHousePostDto(
                guestHousePost.getId(),
                guestHousePost.getGuestHouseName(),
                guestHousePost.getRoadNameAddress(),
                imageUrl,
                false
        );
    }

    public static List<OwnerGuestHousePostDto> of(List<GuestHousePost> guestHousePosts, List<String> imageUrls) {
        List<OwnerGuestHousePostDto> dtos = new ArrayList<>();
        for (int i = 0; i < guestHousePosts.size(); i++) {
            dtos.add(OwnerGuestHousePostDto.of(guestHousePosts.get(i), imageUrls.get(i)));
        }
        return dtos;
    }
}
