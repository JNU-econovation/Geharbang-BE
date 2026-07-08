package guesthouse.staffrecruitment.dto.response;

import guesthouse.staffrecruitment.domain.vo.Gender;
import guesthouse.staffrecruitment.domain.vo.Region;
import guesthouse.staffrecruitment.domain.vo.WorkScheduleType;
import guesthouse.staffrecruitment.domain.vo.WorkType;
import guesthouse.staffrecruitment.domain.vo.WorkingPeriod;
import guesthouse.staffrecruitment.dto.StaffRecruitmentDetailsDTO;
import guesthouse.staffrecruitment.dto.StaffRecruitmentJobDTO;
import lombok.Builder;
import org.locationtech.jts.geom.Point;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Builder
public record StaffRecruitmentDetailsResponse(
        List<String> representativeImages,
        String title,
        String guestHouseName,
        Region region,
        Location location,
        WorkingInformation workingInformation,
        Introduction introduction,
        Feature feature,
        Contact contact,
        Boolean isWished,
        String ownerMessage

) {
    private static final String SEPARATOR = "\\|:\\|";

    public static StaffRecruitmentDetailsResponse from(StaffRecruitmentDetailsDTO details) {
        return StaffRecruitmentDetailsResponse.builder()
                .representativeImages(details.representativeImages())
                .title(details.title())
                .guestHouseName(details.guestHouseName())
                .region(details.region())
                .location(Location.from(details.address(), details.coordinates()))
                .workingInformation(WorkingInformation.from(details.startDate(), details.isStartDateNegotiable(), details.workingPeriod(), details.jobs()))
                .introduction(new Introduction(details.content(), details.contentImages()))
                .feature(Feature.from(details.gender(), details.advantages(), details.employeeBenefits()))
                .contact(new Contact(
                        details.instagramId(),
                        details.phoneNumber(),
                        details.webSite(),
                        details.reservationUrl()
                ))
                .isWished(details.isWished())
                .ownerMessage(details.ownerMessage())
                .build();
    }

    public static record Location(
            String address,
            double[] coordinates
    ) {
        public static Location from(String address, Point coordinates) {
            return new Location(
                    address,
                    new double[]{coordinates.getX(), coordinates.getY()}
            );
        }
    }

    public static record WorkingInformation(
            LocalDate startDate,
            Boolean isStartDateNegotiable,
            WorkingPeriod workingPeriod,
            List<JobSummaryDTO> jobs
    ) {
        public static WorkingInformation from(LocalDate startDate,
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

    public static record JobSummaryDTO(
            String name,
            LocalTime startTIme,
            LocalTime endTime,
            String job,
            Integer workDays,
            Integer restDays,
            WorkType workType,
            WorkScheduleType weeklyWorkingDays
    ) {
        public static JobSummaryDTO from(StaffRecruitmentJobDTO dto) {
            return new JobSummaryDTO(
                    dto.name(),
                    dto.startTIme(),
                    dto.endTime(),
                    dto.job(),
                    dto.workDays(),
                    dto.restDays(),
                    dto.workType(),
                    dto.weeklyWorkingDays()
            );
        }
    }

    public static record Introduction(
            String content,
            List<String> images
    ) {
    }

    public static record Feature(
            Gender gender,
            List<String> advantages,
            List<String> employeeBenefits
    ) {
        public static Feature from(Gender gender, String advantages, String employeeBenefits) {
            List<String> advantagesList = splitBySeparator(advantages);
            List<String> employeeBenefitsList = splitBySeparator(employeeBenefits);

            return new Feature(
                    gender,
                    advantagesList,
                    employeeBenefitsList
            );
        }

        private static List<String> splitBySeparator(String str) {
            List<String> result = new ArrayList<>();

            if (str != null && !str.isBlank())
                result = Arrays.asList(str.split(SEPARATOR));

            return result;
        }
    }

    public static record Contact(
            String instagramId,
            String phoneNumber,
            String webSite,
            String reservationUrl
    ) {
    }
}
