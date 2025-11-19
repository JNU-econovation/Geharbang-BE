package guesthouse.staffrecruitment.dto.response;

import guesthouse.staffrecruitment.domain.vo.Gender;
import guesthouse.staffrecruitment.domain.vo.Region;
import guesthouse.staffrecruitment.domain.vo.WorkingPeriod;
import guesthouse.staffrecruitment.dto.StaffRecruitmentDetailsDTO;
import guesthouse.staffrecruitment.dto.StaffRecruitmentJobDTO;
import lombok.Builder;
import org.locationtech.jts.geom.Point;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Builder
public record StaffRecruitmentDetailsResponse(
        List<String> representativeImages,
        String title,
        String guesthouseName,
        Region region,
        Location location,
        WorkingInformation workingInformation,
        Introduction introduction,
        Feature feature,
        Contact contact,
        Boolean isWished,
        String ownerMessage

) {
    private static final String SEPARATOR = "\\|";

    public static StaffRecruitmentDetailsResponse from(StaffRecruitmentDetailsDTO details) {
        return StaffRecruitmentDetailsResponse.builder()
                .representativeImages(details.representativeImages())
                .title(details.title())
                .guesthouseName(details.guesthouseName())
                .region(details.region())
                .location(Location.from(details.address(), details.coordinates()))
                .workingInformation(WorkingInformation.from(details.startDate(), details.isStartDateNegotiable(), details.workingPeriod(), details.jobs()))
                .introduction(new Introduction(details.content(), details.contentImages()))
                .feature(Feature.from(details.gender(), details.advantages(), details.employeeBenefits()))
                .contact(new Contact(details.instagramId(), details.phoneNumber(), details.email(), details.webSite()))
                .isWished(details.isWished())
                .ownerMessage(details.ownerMessage())
                .build();
    }

    private record Location(
            String address,
            double[] coordinates
    ) {
        private static Location from(String address, Point coordinates) {
            return new Location(
                    address,
                    new double[]{coordinates.getY(),  coordinates.getX()}
            );
        }
    }

    private record WorkingInformation(
            LocalDate startDate,
            Boolean isStartDateNegotiable,
            WorkingPeriod workingPeriod,
            List<JobSummaryDTO> jobs
    ) {
        private static WorkingInformation from(LocalDate startDate,
                                               Boolean isStartDateNegotiable,
                                               WorkingPeriod workingPeriod,
                                               List<StaffRecruitmentJobDTO> jobs) {
            return new WorkingInformation(
                    startDate,
                    isStartDateNegotiable,
                    workingPeriod,
                    jobs.stream()
                            .map(JobSummaryDTO::from)
                            .toList()
            );
        }
    }

    private record JobSummaryDTO(
            String name,
            LocalTime startTIme,
            LocalTime endTime,
            String job,
            Integer workDays,
            Integer restDays
    ) {
        private static JobSummaryDTO from(StaffRecruitmentJobDTO dto) {
            return new JobSummaryDTO(
                    dto.name(),
                    dto.startTIme(),
                    dto.endTime(),
                    dto.job(),
                    dto.workDays(),
                    dto.restDays()
            );
        }
    }

    private record Introduction(
            String content,
            List<String> images
    ) {
    }

    private record Feature(
            Gender gender,
            List<String> advantages,
            List<String> employeeBenefits
    ) {
        public static Feature from(Gender gender, String advantages, String employeeBenefits) {
            List<String> advantagesList = Arrays.asList(advantages.split(SEPARATOR));
            List<String> employeeBenefitsList = Arrays.asList(employeeBenefits.split(SEPARATOR));

            return new Feature(
                    gender,
                    advantagesList,
                    employeeBenefitsList
            );
        }
    }

    private record Contact(
            String instagramId,
            String phoneNumber,
            String email,
            String webSite
    ) {
    }
}
