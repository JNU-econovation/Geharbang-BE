package guesthouse.staffrecruitment.dto.response;

import guesthouse.staffrecruitment.domain.model.Contact;
import guesthouse.staffrecruitment.domain.model.StaffRecruitment;

import java.util.List;

public record StaffRecruitmentMapPostDto(
        Long id,
        String title,
        String guestHouseName,
        String address,
        double[] coordinates,
        String region,
        List<String> representativeImageUrls,
        String webSite,
        String instagramId,
        String phoneNumber,
        String reservationUrl,
        Boolean isWished
) {

    public static StaffRecruitmentMapPostDto of(
            StaffRecruitment recruitment,
            List<String> representativeImageUrls,
            Boolean isWished
    ) {
        Contact contact = recruitment.getContact();

        return new StaffRecruitmentMapPostDto(
                recruitment.getId(),
                recruitment.getTitle(),
                recruitment.getGuestHouseName(),
                getAddress(recruitment.getRoadNameAddress(), recruitment.getLotNumberAddress()),
                new double[]{recruitment.getCoordinates().getX(), recruitment.getCoordinates().getY()},
                recruitment.getRegion().name(),
                representativeImageUrls,
                contact == null ? "" : contact.getWebSite(),
                contact == null ? "" : contact.getInstagramId(),
                contact == null ? "" : contact.getPhoneNumber(),
                contact == null ? "" : contact.getReservationUrl(),
                isWished
        );
    }

    private static String getAddress(String roadNameAddress, String lotNumberAddress) {
        if (roadNameAddress != null && !roadNameAddress.isBlank()) {
            return roadNameAddress;
        }
        return lotNumberAddress;
    }
}
