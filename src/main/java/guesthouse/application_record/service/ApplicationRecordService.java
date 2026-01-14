package guesthouse.application_record.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guesthouse.application.domain.model.Application;
import guesthouse.application.domain.model.QuestionAnswer;
import guesthouse.application.dto.ApplicationSnapShotDTO;
import guesthouse.application.dto.QuestionAnswerDTO;
import guesthouse.application.exception.SnapShotException;
import guesthouse.application.repository.QuestionAnswerRepository;
import guesthouse.application.service.ApplicationService;
import guesthouse.application_record.domain.model.ApplicationRecord;
import guesthouse.application_record.dto.response.*;
import guesthouse.application_record.exception.ApplicationRecordErrorCode;
import guesthouse.application_record.exception.ApplicationRecordException;
import guesthouse.application_record.repository.ApplicationRecordRepository;
import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import guesthouse.staffrecruitment.domain.model.StaffRecruitmentQuestion;
import guesthouse.staffrecruitment.exception.StaffRecruitmentErrorCode;
import guesthouse.staffrecruitment.exception.StaffRecruitmentException;
import guesthouse.staffrecruitment.repository.StaffRecruitmentRepository;
import guesthouse.staffrecruitment.service.StaffRecruitmentService;
import guesthouse.user.domain.model.User;
import guesthouse.user.repository.UserRepository;
import guesthouse.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationRecordService {

    private final ObjectMapper objectMapper;
    private final ApplicationService applicationService;
    private final StaffRecruitmentService staffRecruitmentService;
    private final ApplicationRecordRepository applicationRecordRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final StaffRecruitmentRepository staffRecruitmentRepository;
    private final UserService userService;
    private final UserRepository userRepository;


    @Transactional
    public void apply(List<QuestionAnswerDTO> answers, Long recruitmentId, Long userId) {
        checkDuplicatedApplication(recruitmentId, userId);
        Application application = applicationService.getApplication(userId);
        String snapShot = convertToSnapshot(application);

        ApplicationRecord record = applicationRecordRepository.save(new ApplicationRecord(recruitmentId, userId, snapShot));
        if (hasQuestions(recruitmentId)) {
            saveQuestionAnswers(answers, recruitmentId, record.getId());
        }
    }

    private void checkDuplicatedApplication(Long recruitmentId, Long userId) {
        Boolean isDuplicated = applicationRecordRepository.existsByStaffRecruitmentIdAndUserId(recruitmentId, userId);
        if (isDuplicated)
            throw new IllegalArgumentException("이미 지원한 스탭 공고입니다");
    }

    private String convertToSnapshot(Application application) {
        ApplicationSnapShotDTO snapShot = ApplicationSnapShotDTO.from(application);
        try {
            return objectMapper.writeValueAsString(snapShot);
        } catch (JsonProcessingException e) {
            throw new SnapShotException();
        }
    }

    private ApplicationSnapShotDTO convertToSnapshot(String applicationSnapShot) {
        try {
            return objectMapper.readValue(applicationSnapShot, ApplicationSnapShotDTO.class);
        } catch (JsonProcessingException e) {
            throw new ApplicationRecordException(ApplicationRecordErrorCode.DESERIALIZATION_FAILED);
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


    public SubmittedApplicationsResponse getApplicationRecords(Long userId, Long staffRecruitmentId) {
        validateCanRead(userId, staffRecruitmentId);

        StaffRecruitment staffRecruitment = staffRecruitmentRepository.findById(staffRecruitmentId)
                .orElseThrow(() -> new StaffRecruitmentException(StaffRecruitmentErrorCode.NOT_FOUND));

        List<ApplicationRecord> applicationRecords = applicationRecordRepository.findAllByStaffRecruitmentId(staffRecruitmentId);
        List<User> users = applicationRecords.stream()
                .map(a -> userService.findById(a.getUserId()))
                .toList();
        return new SubmittedApplicationsResponse(staffRecruitment.getTitle(), SubmittedApplicationDto.of(applicationRecords, users));
    }

    public ApplicationRecordResponse getApplicationRecord(Long userId, Long applicationRecordId) {
        ApplicationRecord applicationRecord = applicationRecordRepository.findByIdOrThrow(applicationRecordId);
        validateCanRead(userId, applicationRecord.getStaffRecruitmentId());

        User user = userService.findById(applicationRecord.getUserId());
        ApplicationSnapShotDTO applicationSnapShotDTO = convertToSnapshot(applicationRecord.getApplicationSnapShot());

        return ApplicationRecordResponse.of(applicationSnapShotDTO, user);
    }

    public ApplicationRecordQuestionsResponse getApplicationRecordQuestions(Long userId, Long applicationRecordId) {
        ApplicationRecord applicationRecord = applicationRecordRepository.findByIdOrThrow(applicationRecordId);
        validateCanRead(userId, applicationRecord.getStaffRecruitmentId());

        List<QuestionAnswer> questionAnswers = questionAnswerRepository.findAllByApplicationRecordId(applicationRecordId);

        List<ApplicationRecordQuestionDto> dtos = questionAnswers.stream()
                .map(qa -> new ApplicationRecordQuestionDto(qa.getQuestion(), qa.getContent()))
                .sorted(Comparator.comparing(ApplicationRecordQuestionDto::question))
                .toList();
        return new ApplicationRecordQuestionsResponse(dtos);
    }

    private void validateCanRead(Long userId, Long applicationRecord) {
        if (!staffRecruitmentRepository.existsByOwnerIdAndId(userId, applicationRecord)) {
            throw new ApplicationRecordException(ApplicationRecordErrorCode.NOT_ALLOWED);
        }
    }

    public void approveApplicationRecord(Long userId, Long applicationRecordId) {
        ApplicationRecord applicationRecord = applicationRecordRepository.findByIdOrThrow(applicationRecordId);
        validateCanRead(userId, applicationRecord.getStaffRecruitmentId());

        applicationRecord.approve();
    }
}
