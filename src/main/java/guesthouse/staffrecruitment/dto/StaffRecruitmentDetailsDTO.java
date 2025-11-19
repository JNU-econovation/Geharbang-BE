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
        String guesthouseName,
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
        String email,
        String webSite,
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
                .guesthouseName(recruitment.getGuesthouseName())
                .region(recruitment.getRegion())
                .address(recruitment.getAddress())
                .coordinates(recruitment.getCoordinates())
                .startDate(recruitment.getStartDate())
                .isStartDateNegotiable(recruitment.getIsStartDateNegotiable())
                .workingPeriod(recruitment.getWorkingPeriod())
                .content(recruitment.getContent())
                .gender(recruitment.getGender())
                .advantages(recruitment.getAdvantages())
                .employeeBenefits(recruitment.getEmployeeBenefits())
                .instagramId(recruitment.getInstagramId())
                .phoneNumber(recruitment.getPhoneNumber())
                .email(recruitment.getEmail())
                .webSite(recruitment.getWebSite())
                .isWished(isWished)
                .ownerMessage(recruitment.getOwnerMessage())
                .build();
    }
}
