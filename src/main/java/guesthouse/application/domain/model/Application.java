package guesthouse.application.domain.model;

import guesthouse.application.domain.vo.DayOfWeek;
import guesthouse.application.domain.vo.Mbti;
import guesthouse.application.domain.vo.Style;
import guesthouse.user.domain.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate availableStartDate;

    @ElementCollection
    @JoinTable(
            name = "available_day_of_week",
            joinColumns = @JoinColumn(name="application_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "value")
    private Set<DayOfWeek> availableDayOfWeek = new HashSet<>();

    @Column(columnDefinition = "TEXT", nullable = false)
    private String selfIntroduction;

    @Enumerated(EnumType.STRING)
    private Mbti mbti;

    @ElementCollection
    @JoinTable(
            name = "style",
            joinColumns = @JoinColumn(name="application_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "value")
    private Set<Style> style;

    private String instagramId;

    private String imageUrl;

    public void update(LocalDate availableStartDate,
                       Set<DayOfWeek> availableDayOfWeek,
                       String selfIntroduction,
                       Mbti mbti,
                       Set<Style> style,
                       String instagramId,
                       String imageUrl) {
        this.availableStartDate = availableStartDate;
        this.availableDayOfWeek = availableDayOfWeek;
        this.selfIntroduction = selfIntroduction;
        this.mbti = mbti;
        this.style = style;
        this.instagramId = instagramId;
        this.imageUrl = imageUrl;
    }
}
