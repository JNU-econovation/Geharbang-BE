package guesthouse.application.dto.requset;

import guesthouse.application.dto.QuestionAnswerDTO;
import jakarta.validation.Valid;

import java.util.List;

public record ApplicationApplyRequest(
        @Valid
        List<QuestionAnswerDTO> answers
) {
}
