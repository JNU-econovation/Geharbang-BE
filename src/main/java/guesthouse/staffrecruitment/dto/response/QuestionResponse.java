package guesthouse.staffrecruitment.dto.response;

import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import guesthouse.staffrecruitment.domain.model.StaffRecruitmentQuestion;

import java.util.List;

public record QuestionResponse(
        GuesthouseDTO guesthouseDTO,
        ProfileDTO profileDTO,
        List<QuestionDTO> questionDTOS
) {

    public record GuesthouseDTO(
            String name,
            String region,
            List<String> tags
    ) {
    }

    public record ProfileDTO(
            String name,
            String imageUrl
    ) {
    }

    public record QuestionDTO(
            Long questionsId,
            String content
    ) {
    }

    public static QuestionResponse of(
            StaffRecruitment staffRecruitment,
            String username, String imageUrl,
            List<StaffRecruitmentQuestion> questions
    ) {
        GuesthouseDTO guesthouseDTO = new GuesthouseDTO(
                staffRecruitment.getGuesthouseName(),
                staffRecruitment.getRegion().name(),
                List.of(staffRecruitment.getWorkingPeriod().name())
        );

        ProfileDTO profileDTO = new ProfileDTO(username, imageUrl);

        List<QuestionDTO> questionDTOs = questions.stream()
                .map(question -> new QuestionDTO(question.getId(), question.getContent()))
                .toList();

        return new QuestionResponse(guesthouseDTO, profileDTO, questionDTOs);
    }
}
