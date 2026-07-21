package guesthouse.staffrecruitment.random;

import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import lombok.Builder;

import java.util.List;

@Builder
public record RandomStaffRecruitmentPostDto(
        Long id,
        String name,
        String address,
        List<String> tags,
        String imageUrl
) {

    public static RandomStaffRecruitmentPostDto of(StaffRecruitment staffRecruitment, String imageUrl) {
        return new RandomStaffRecruitmentPostDto(
                staffRecruitment.getId(),
                staffRecruitment.getGuestHouseName(),
                getAddress(staffRecruitment.getRoadNameAddress(), staffRecruitment.getLotNumberAddress()),
                List.of(staffRecruitment.getWorkingPeriod().name()),
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
