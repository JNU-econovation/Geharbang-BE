package guesthouse.staffrecruitment.domain.model;

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
    private Integer workDays;
    private Integer restDays;

}
