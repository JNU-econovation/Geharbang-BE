package guesthouse.user.domain.vo;

import guesthouse.user.exception.UserErrorCode;
import guesthouse.user.exception.UserException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonalInfo {

    private String name;

    @Column(unique = true)
    private String phoneNumber;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    public PersonalInfo(String name, String phoneNumber, LocalDate birthDate, Gender gender){
        validateName(name);
        validatePhoneNumber(phoneNumber);
        validateBirthDate(birthDate);
        validateGender(gender);
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new UserException(UserErrorCode.NAME_REQUIRED);
        }
    }

    private void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new UserException(UserErrorCode.PHONE_NUMBER_REQUIRED);
        }
    }

    private void validateBirthDate(LocalDate birthDate) {
        if (birthDate == null) {
            throw new UserException(UserErrorCode.BIRTH_DATE_REQUIRED);
        }
    }

    private void validateGender(Gender gender) {
        if (gender == null) {
            throw new UserException(UserErrorCode.GENDER_REQUIRED);
        }
    }
}
