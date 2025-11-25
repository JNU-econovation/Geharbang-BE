package guesthouse.staffrecruitment.domain.vo;

import lombok.Getter;

@Getter
public enum WorkScheduleType {
    주1일(1),
    주2일(2),
    주3일(3),
    주4일(4),
    주5일(5),
    ;

    private final int workDays;

    WorkScheduleType(int workDays) {
        this.workDays = workDays;
    }
}
