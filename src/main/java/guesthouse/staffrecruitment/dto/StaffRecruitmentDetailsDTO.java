package guesthouse.staffrecruitment.dto;

import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import guesthouse.staffrecruitment.domain.model.StaffRecruitmentJob;
import guesthouse.staffrecruitment.domain.vo.Gender;
import guesthouse.staffrecruitment.domain.vo.Region;
import guesthouse.staffrecruitment.domain.vo.WorkingPeriod;
import lombok.Builder;
import org.locationtech.jts.geom.Point;

import java.time.LocalDate;
import java.util.List;

@Builder
public record StaffRecruitmentDetailsDTO(
        List<String> representativeImages,

        String title,
        String guestHouseName,
        Region region,
        String address,
        Point coordinates,

        LocalDate startDate,
        Boolean isStartDateNegotiable,
        WorkingPeriod workingPeriod,
        List<StaffRecruitmentJobDTO> jobs,

        String content,
        List<String> contentImages,

        Gender gender,
        String advantages,
        String employeeBenefits,

        String instagramId,
        String phoneNumber,
        String webSite,
        String reservationUrl,
        Boolean isWished,
        String ownerMessage
) {
    public static StaffRecruitmentDetailsDTO from (StaffRecruitment recruitment,
                                                   List<StaffRecruitmentJob> jobs,
                                                   List<String> representativeImages,
                                                   List<String> contentImages,
                                                   Boolean isWished
    ) {

        return StaffRecruitmentDetailsDTO.builder()
                .representativeImages(representativeImages)
                .contentImages(contentImages)
                .jobs(jobs
                        .stream()
                        .map(StaffRecruitmentJobDTO::from)
                        .toList())
                .title(recruitment.getTitle())
                .guestHouseName(recruitment.getGuestHouseName())
                .region(recruitment.getRegion())
                .address(recruitment.getLotNumberAddress())
                .coordinates(recruitment.getCoordinates())
                .startDate(recruitment.getStartDate())
                .isStartDateNegotiable(false)
                .workingPeriod(recruitment.getWorkingPeriod())
                .content(recruitment.getContent())
                .gender(recruitment.getFeature().getGender())
                .advantages(recruitment.getFeature().getAdvantages())
                .employeeBenefits(recruitment.getFeature().getEmployeeBenefits())
                .instagramId(recruitment.getContact().getInstagramId())
                .phoneNumber(recruitment.getContact().getPhoneNumber())
                .webSite(recruitment.getContact().getWebSite())
                .reservationUrl(recruitment.getContact().getReservationUrl())
                .isWished(isWished)
                .ownerMessage(recruitment.getOwnerMessage())
                .build();
    }
}
