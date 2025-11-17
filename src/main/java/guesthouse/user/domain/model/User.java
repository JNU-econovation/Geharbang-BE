package guesthouse.user.domain.model;

import guesthouse.user.domain.vo.Gender;
import guesthouse.user.domain.vo.PersonalInfo;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "uuser")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private PersonalInfo personalInfo;

    public User() {
    }

    public void updatePersonalInfo(String name, String phoneNumber, LocalDate birthDate, Gender gender) {
        this.personalInfo = new PersonalInfo(name, phoneNumber, birthDate, gender);
    }
}
