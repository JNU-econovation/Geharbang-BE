package guesthouse.guestHousePost.dto.response;

import guesthouse.guestHousePost.domain.model.GuestHousePost;
import guesthouse.guestHousePost.domain.vo.Contact;

import java.util.List;

public record GuestHouseMapPostDto(
        Long id,
        String guestHouseName,
        String address,
        double[] coordinates,
        String region,
        List<String> imageUrls,
        String webSite,
        String instagramId,
        String phoneNumber,
        String reservationUrl
) {

    public static GuestHouseMapPostDto of(GuestHousePost post, List<String> imageUrls) {
        Contact contact = post.getContact();

        return new GuestHouseMapPostDto(
                post.getId(),
                post.getGuestHouseName(),
                getAddress(post.getRoadNameAddress(), post.getLotNumberAddress()),
                new double[]{post.getCoordinates().getX(), post.getCoordinates().getY()},
                post.getRegion().name(),
                imageUrls,
                contact == null ? "" : contact.getWebSite(),
                contact == null ? "" : contact.getInstagramId(),
                contact == null ? "" : contact.getPhoneNumber(),
                contact == null ? "" : contact.getReservationUrl()
        );
    }

    private static String getAddress(String roadNameAddress, String lotNumberAddress) {
        if (roadNameAddress != null && !roadNameAddress.isBlank()) {
            return roadNameAddress;
        }
        return lotNumberAddress;
    }
}
