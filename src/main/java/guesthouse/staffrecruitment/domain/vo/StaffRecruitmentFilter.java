package guesthouse.staffrecruitment.domain.vo;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class StaffRecruitmentFilter {

    private final String keyword;
    private final SortType sortType;
    private final List<Region> region;
    private final WorkType workType;
    private final Integer workDays;
    private final Integer restDays;
    private final Gender gender;
    private final List<WorkingPeriod> workingPeriods;
    private final List<WorkScheduleType> workScheduleType;

    public StaffRecruitmentFilter(String keyword, SortType sortType, List<Region> region,
                                  WorkType workType, Integer workDays, Integer restDays,
                                  Gender gender, List<WorkingPeriod> workingPeriods, List<WorkScheduleType> workScheduleType) {
        this.keyword = keyword;
        this.sortType = sortType;
        this.region = region;
        this.workType = workType;
        this.workDays = workDays;
        this.restDays = restDays;
        this.gender = gender;
        this.workingPeriods = workingPeriods;
        this.workScheduleType = workScheduleType;
    }
}
