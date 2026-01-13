package guesthouse.staffrecruitment.dto.response;

import guesthouse.staffrecruitment.domain.model.StaffRecruitment;

import java.util.ArrayList;
import java.util.List;

public record OwnerStaffRecruitmentPostDto(
        Long id,
        String title,
        String roadNameAddress,
        String imageUrl,
        boolean isClosed
) {

    public static OwnerStaffRecruitmentPostDto of(StaffRecruitment staffRecruitment, String imageUrl) {
        return new OwnerStaffRecruitmentPostDto(
                staffRecruitment.getId(),
                staffRecruitment.getTitle(),
                staffRecruitment.getRoadNameAddress(),
                imageUrl,
                false
        );
    }

    public static List<OwnerStaffRecruitmentPostDto> of(List<StaffRecruitment> staffRecruitments, List<String> imageUrls) {
        List<OwnerStaffRecruitmentPostDto> dtos = new ArrayList<>();
        for (int i = 0; i < staffRecruitments.size(); i++) {
            dtos.add(of(staffRecruitments.get(i), imageUrls.get(i)));
        }
        return dtos;
    }
}
