package guesthouse.application_record.dto.response;

import java.util.List;

public record ApplicationRecordQuestionsResponse(
        List<ApplicationRecordQuestionDto> questions
) {
}
