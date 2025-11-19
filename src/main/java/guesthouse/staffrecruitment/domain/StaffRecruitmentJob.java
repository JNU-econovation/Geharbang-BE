package guesthouse.staffrecruitment.domain;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
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
