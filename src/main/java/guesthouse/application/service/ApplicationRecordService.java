package guesthouse.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guesthouse.application.domain.model.Application;
import guesthouse.application.domain.model.ApplicationRecord;
import guesthouse.application.domain.model.QuestionAnswer;
import guesthouse.application.dto.ApplicationSnapShotDTO;
import guesthouse.application.dto.QuestionAnswerDTO;
import guesthouse.application.exception.SnapShotException;
import guesthouse.application.repository.ApplicationRecordRepository;
import guesthouse.application.repository.QuestionAnswerRepository;
import guesthouse.staffrecruitment.domain.model.StaffRecruitmentQuestion;
import guesthouse.staffrecruitment.service.StaffRecruitmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationRecordService {

    private final ObjectMapper objectMapper;
    private final ApplicationService applicationService;
    private final StaffRecruitmentService staffRecruitmentService;
    private final ApplicationRecordRepository applicationRecordRepository;
    private final QuestionAnswerRepository questionAnswerRepository;

    @Transactional
    public void apply(List<QuestionAnswerDTO> answers, Long recruitmentId, Long userId) {
        Application application = applicationService.getApplication(userId);
        String snapShot = convertToSnapshot(application);

        ApplicationRecord record= applicationRecordRepository.save(new ApplicationRecord(recruitmentId, userId, snapShot));
        if(hasQuestions(recruitmentId)) {
            saveQuestionAnswers(answers, recruitmentId, record.getId());
        }
    }

    private String convertToSnapshot(Application application) {
        ApplicationSnapShotDTO  snapShot = ApplicationSnapShotDTO.from(application);
        try {
            return objectMapper.writeValueAsString(snapShot);
        } catch (JsonProcessingException e) {
            throw new SnapShotException();
        }
    }

    private boolean hasQuestions(Long recruitmentId) {
        return staffRecruitmentService.hasQuestion(recruitmentId);
    }

    private void saveQuestionAnswers(List<QuestionAnswerDTO> answers, Long recruitmentId, Long recordId) {
        answers.forEach(answer -> {
           StaffRecruitmentQuestion question = staffRecruitmentService.getStaffRecruitmentQuestion(answer.questionId(), recruitmentId);
           saveQuestionAnswer(recordId, question.getContent(), answer.content());
        });
    }

    private void saveQuestionAnswer(Long recordId, String question, String content) {
        questionAnswerRepository.save(new QuestionAnswer(recordId, question, content));
    }
}
