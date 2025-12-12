package guesthouse.staffrecruitment.domain.model;

import guesthouse.staffrecruitment.domain.vo.WorkScheduleType;
import guesthouse.staffrecruitment.domain.vo.WorkType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Builder
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StaffRecruitmentJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long staffRecruitmentId;

    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String job;

    @Enumerated(EnumType.STRING)
    private WorkType standard;
    private Integer workDays;
    private Integer restDays;

    @Enumerated(EnumType.STRING)
    private WorkScheduleType workScheduleType;

}
