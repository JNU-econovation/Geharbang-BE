package guesthouse.user.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonalInfo {
    private static final String NAME_REQUIRED_MESSAGE = "유저 이름은 null이거나 비어있을 수 없습니다";
    private static final String PHONE_NUMBER_REQUIRED_MESSAGE = "휴대폰 번호는 null이거나 비어있을 수 없습니다";
    private static final String BIRTH_DATE_REQUIRED_MESSAGE = "출생년도는 null일 수 없습니다";
    private static final String GENDER_REQUIRED_MESSAGE = "성별은 null일 수 없습니다";

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
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
            throw new IllegalArgumentException(NAME_REQUIRED_MESSAGE);
        }
    }

    private void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException(PHONE_NUMBER_REQUIRED_MESSAGE);
        }
    }

    private void validateBirthDate(LocalDate birthDate) {
        if (birthDate == null) {
            throw new IllegalArgumentException(BIRTH_DATE_REQUIRED_MESSAGE);
        }
    }

    private void validateGender(Gender gender) {
        if (gender == null) {
            throw new IllegalArgumentException(GENDER_REQUIRED_MESSAGE);
        }
    }
}
