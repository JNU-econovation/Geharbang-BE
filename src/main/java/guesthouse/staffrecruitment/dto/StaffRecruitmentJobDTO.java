package guesthouse.staffrecruitment.dto;

import guesthouse.staffrecruitment.domain.model.StaffRecruitmentJob;
import lombok.Builder;

import java.time.LocalTime;

@Builder
public record StaffRecruitmentJobDTO(
        Long id,
        String name,
        LocalTime startTIme,
        LocalTime endTime,
        String job,
        Integer workDays,
        Integer restDays
) {
    public static StaffRecruitmentJobDTO from(StaffRecruitmentJob job) {
        return StaffRecruitmentJobDTO.builder()
                .id(job.getId())
                .name(job.getName())
                .startTIme(job.getStartTime())
                .endTime(job.getEndTime())
                .job(job.getJob())
                .workDays(job.getWorkDays())
                .restDays(job.getRestDays())
                .build();
    }
}
