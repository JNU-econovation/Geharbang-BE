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
        return StaffRecruitmentJob.builder()
                .staffRecruitmentId(staffRecruitmentId)
                .name(job.name())
                .startTime(job.startTime())
                .endTime(job.endTime())
                .job(job.job())
                .standard(job.standard())
                .workDays(job.workDays())
                .restDays(job.restDays())
                .workScheduleType(job.weeklyWorkingDays())
                .build();
    }
}
