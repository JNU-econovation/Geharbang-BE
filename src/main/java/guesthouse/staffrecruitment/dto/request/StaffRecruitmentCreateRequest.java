package guesthouse.staffrecruitment.dto.request;

import guesthouse.staffrecruitment.domain.vo.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record StaffRecruitmentCreateRequest(

        @NotBlank
        @Length(min = 5, max = 30)
        String title,

        @NotBlank
        @Length(min = 2, max = 30)
        String guestHouseName,

        @NotNull
        Region region,

        @Valid
        @NotNull
        Location location,

        @NotEmpty
        @Size(min = 1, max = 10)
        List<String> representativeImageUrls,

        @Valid
        @NotNull
        WorkingInformation workingInformation,

        @Valid
        @NotNull
        Feature feature,

        @Valid
        @NotNull
        Introduction introduction,

        @NotNull
        Contact contact,

        String ownerMessage,

        List<String> questions

) {

    public record Location(

            @NotBlank
            String lotNumberAddress,

            String roadNameAddress,

            @NotNull
            @Size(min = 2, max = 2)
            List<Double> coordinates
    ) {
    }


    public record WorkingInformation(

            @NotBlank
            LocalDate startDate,

            @NotBlank
            WorkingPeriod workingPeriod,

            @NotEmpty
            List<Job> jobs
    ) {
    }

    public record Job(

            @NotBlank
            @Size(min = 1, max = 20)
            String name,

            @NotNull
            LocalTime startTime,

            @NotNull
            LocalTime endTime,

            @NotBlank
            @Size(min = 1, max = 50)
            String job,

            @NotNull
            WorkType standard,

            Integer workDays,
            Integer restDays,
            WorkScheduleType weeklyWorkingDays
    ) {
    }

    public record Feature(

            @NotNull
            Gender gender,

            List<String> advantages,

            List<String> employeeBenefits
    ) {
    }

    public record Introduction(

            @NotBlank
            String content,

            @NotEmpty
            @Size(min = 1, max = 10)
            List<String> imageUrls
    ) {
    }

    public record Contact(

            String phoneNumber,

            String instagramId,

            String webSite
    ) {
    }

}
