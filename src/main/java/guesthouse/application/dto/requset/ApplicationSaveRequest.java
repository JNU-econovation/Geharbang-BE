package guesthouse.application.dto.requset;

import guesthouse.common.annotation.ValidPhoneNumberPattern;
import guesthouse.common.exception.message.ValidationMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record ApplicationSaveRequest(
        @NotBlank
        String name,

        @NotBlank
        @ValidPhoneNumberPattern
        String phoneNumber,

        @NotNull
        LocalDate birthDate,

        @NotBlank
        String gender,

        @NotNull
        LocalDate availableStartDate,

        @NotNull
        List<String> availableDayOfWeek,

        @NotBlank
        @Size(max = 300, message = ValidationMessage.SELF_INTRODUCTION_LENGTH_EXCEEDED)
        String selfIntroduction,

        @NotBlank
        String mbti,

        @NotNull
        List<String> style,

        String instagramId,

        @NotBlank
        String imageUrl
) {
}
