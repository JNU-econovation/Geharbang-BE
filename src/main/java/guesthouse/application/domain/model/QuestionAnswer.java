package guesthouse.application.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long applicationRecordId;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false)
    private String content;

    public QuestionAnswer(Long applicationRecordId, String question, String content) {
        this.applicationRecordId = applicationRecordId;
        this.question = question;
        this.content = content;
    }
}
