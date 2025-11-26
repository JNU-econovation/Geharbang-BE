package guesthouse.staffrecruitment.dto.response;

import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import guesthouse.staffrecruitment.domain.model.StaffRecruitmentQuestion;

import java.util.List;

public record QuestionResponse(
        GuesthouseDTO guesthouse,
        ProfileDTO profile,
        List<QuestionDTO> questions
) {

    public record GuesthouseDTO(
            String name,
            String region,
            List<String> tags,
            String imageUrl
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
            String staffRecruitmentImageUrl,
            String username,
            String userImageUrl,
            List<StaffRecruitmentQuestion> questions
    ) {
        GuesthouseDTO guesthouseDTO = new GuesthouseDTO(
                staffRecruitment.getGuesthouseName(),
                staffRecruitment.getRegion().name(),
                List.of(staffRecruitment.getWorkingPeriod().name()),
                staffRecruitmentImageUrl
        );

        ProfileDTO profileDTO = new ProfileDTO(username, userImageUrl);

        List<QuestionDTO> questionDTOs = questions.stream()
                .map(question -> new QuestionDTO(question.getId(), question.getContent()))
                .toList();

        return new QuestionResponse(guesthouseDTO, profileDTO, questionDTOs);
    }
}
