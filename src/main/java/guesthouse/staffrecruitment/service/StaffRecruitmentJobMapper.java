package guesthouse.staffrecruitment.service;

import guesthouse.staffrecruitment.domain.model.StaffRecruitmentJob;
import guesthouse.staffrecruitment.domain.vo.WorkType;
import guesthouse.staffrecruitment.dto.request.StaffRecruitmentCreateRequest;

import java.util.List;

public final class StaffRecruitmentJobMapper {
    private static final Integer SEVEN_DAYS = 7;

    private StaffRecruitmentJobMapper() {

    }

    public static List<StaffRecruitmentJob> from(StaffRecruitmentCreateRequest request, Long staffRecruitmentId) {
        List<StaffRecruitmentCreateRequest.Job> jobs = request.workingInformation().jobs();

        return jobs.stream()
                .map(job -> createStaffRecruitmentJob(staffRecruitmentId, job))
                .toList();
    }

    private static StaffRecruitmentJob createStaffRecruitmentJob(Long staffRecruitmentId, StaffRecruitmentCreateRequest.Job job) {
        Integer workDays = job.workDays();
        Integer restDays = job.restDays();
        if (isWeeklyStandard(job.standard())){
            workDays = job.weeklyWorkingDays().getWorkDays();
            restDays = SEVEN_DAYS - workDays;
        }

        return StaffRecruitmentJob.builder()
                .staffRecruitmentId(staffRecruitmentId)
                .name(job.name())
                .startTime(job.startTime())
                .endTime(job.endTime())
                .job(job.job())
                .standard(job.standard())
                .workDays(workDays)
                .restDays(restDays)
                .workScheduleType(job.weeklyWorkingDays())
                .build();
    }

    private static boolean isWeeklyStandard(WorkType standard) {
        return standard == WorkType._7일_기준;
    }
}
