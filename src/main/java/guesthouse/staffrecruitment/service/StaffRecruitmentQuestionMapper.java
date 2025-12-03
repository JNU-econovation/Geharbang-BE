package guesthouse.staffrecruitment.service;

import guesthouse.staffrecruitment.domain.model.StaffRecruitmentQuestion;
import guesthouse.staffrecruitment.dto.request.StaffRecruitmentCreateRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class StaffRecruitmentQuestionMapper {

    private StaffRecruitmentQuestionMapper() {

    }

    public static List<StaffRecruitmentQuestion> from(StaffRecruitmentCreateRequest request, Long staffRecruitmentId) {
        return Optional.ofNullable(request.questions())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(question -> createQuestion(staffRecruitmentId, question))
                .toList();
    }

    private static StaffRecruitmentQuestion createQuestion(Long staffRecruitmentId, String question) {
        return StaffRecruitmentQuestion.builder()
                .staffRecruitmentId(staffRecruitmentId)
                .content(question)
                .build();
    }
}
