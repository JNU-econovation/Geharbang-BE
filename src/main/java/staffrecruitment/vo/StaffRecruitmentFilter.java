package staffrecruitment.vo;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class StaffRecruitmentFilter {

    private final String keyword;
    private final SortType sortType;
    private final List<Region> region;
    private final List<WorkingPeriod> workingPeriods;
    private final Gender gender;
    private final List<WorkScheduleType> workScheduleType;

    public StaffRecruitmentFilter(String keyword, SortType sortType, List<Region> region, Gender gender, List<WorkingPeriod> workingPeriods, List<WorkScheduleType> workScheduleType) {
        this.keyword = keyword;
        this.sortType = sortType;
        this.region = region;
        this.gender = gender;
        this.workingPeriods = workingPeriods;
        this.workScheduleType = workScheduleType;
    }
}
