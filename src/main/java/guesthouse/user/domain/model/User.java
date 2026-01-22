package guesthouse.user.domain.model;

import guesthouse.common.domain.TimeEntity;
import guesthouse.user.domain.vo.Gender;
import guesthouse.user.domain.vo.PersonalInfo;
import guesthouse.user.domain.vo.Role;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "uuser")
public class User extends TimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private PersonalInfo personalInfo;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    private Role role;

    public User() {
        this.role = Role.사용자;
    }

    public void updatePersonalInfo(String name, String phoneNumber, LocalDate birthDate, Gender gender, String profileImageUrl) {
        this.personalInfo = new PersonalInfo(name, phoneNumber, birthDate, gender);
        this.profileImageUrl = profileImageUrl;
    }

    public String getProfileImageUrl() {
        if (profileImageUrl == null || profileImageUrl.isBlank()) {
            return "";
        }
        return profileImageUrl;
    }

    public boolean isAdmin() {
        return this.role == Role.운영자;
    }
}
