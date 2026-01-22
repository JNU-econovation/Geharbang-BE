package guesthouse.user.service;

import guesthouse.certificate.domain.model.Certificate;
import guesthouse.certificate.domain.vo.Status;
import guesthouse.certificate.repository.CertificateRepository;
import guesthouse.user.domain.model.User;
import guesthouse.user.domain.vo.Gender;
import guesthouse.user.dto.ProfileDTO;
import guesthouse.user.exception.UserErrorCode;
import guesthouse.user.exception.UserException;
import guesthouse.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CertificateRepository certificateRepository;

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND));
    }

    public void updatePersonalInfo(String name,
                                   String phoneNumber,
                                   LocalDate birthDate,
                                   Gender gender,
                                   Long userId) {
        User user = findById(userId);
        user.updatePersonalInfo(name, phoneNumber, birthDate, gender);
    }

    @Transactional(readOnly = true)
    public ProfileDTO getProfile(Long userId) {
        User user = findById(userId);
        Boolean isOwner = certificateRepository.existsByUserId(userId);
        List<Certificate> certificates = certificateRepository.findByUserId(userId);

        boolean hasInReview = certificates.stream()
                .anyMatch(certificate -> certificate.getStatus().isInReview());

        return new ProfileDTO(
                user.getPersonalInfo().getName(),
                user.getProfileImageUrl(),
                isOwner,
                hasInReview
        );
    }

    public void validateAdmin(Long userId) {
        User user = findById(userId);
        if (!user.isAdmin())
            throw new UserException(UserErrorCode.NOT_ADMIN);
    }
}
