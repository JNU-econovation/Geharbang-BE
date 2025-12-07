package guesthouse.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record QuestionAnswerDTO(
        @Positive
        Long questionId,

        @NotNull
        @NotBlank
        String content
) {
}
