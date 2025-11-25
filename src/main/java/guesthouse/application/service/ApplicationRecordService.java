package guesthouse.application.service;

import guesthouse.application.domain.model.ApplicationRecord;
import guesthouse.application.domain.model.QuestionAnswer;
import guesthouse.application.dto.QuestionAnswerDTO;
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

    private final StaffRecruitmentService staffRecruitmentService;
    private final ApplicationRecordRepository applicationRecordRepository;
    private final QuestionAnswerRepository questionAnswerRepository;

    @Transactional
    public void apply(List<QuestionAnswerDTO> answers, Long recruitmentId, Long userId) {
        ApplicationRecord record= applicationRecordRepository.save(new ApplicationRecord(recruitmentId, userId));
        if(hasQuestions(recruitmentId)) {
            saveQuestionAnswers(answers, recruitmentId, record.getId());
        }
    }

    private boolean hasQuestions(Long recruitmentId) {
        return staffRecruitmentService.hasQuestion(recruitmentId);
    }

    private void saveQuestionAnswers(List<QuestionAnswerDTO> answers, Long recruitmentId, Long recordId) {
        answers.forEach(answer -> {
           StaffRecruitmentQuestion question = staffRecruitmentService.getStaffRecruitmentQuestion(answer.questionId(), recruitmentId);
           saveQuestionAnswer(recordId, question.getId(), answer.content());
        });
    }

    private void saveQuestionAnswer(Long recordId, Long questionId, String content) {
        questionAnswerRepository.save(new QuestionAnswer(recordId, questionId, content));
    }
}
