package guesthouse.user.service;

import guesthouse.certificate.domain.model.Certificate;
import guesthouse.certificate.domain.vo.Status;
import guesthouse.certificate.service.CertificateService;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CertificateService certificateService;

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
        Boolean isOwner =certificateService.isOwner(userId);
        Boolean inReview = certificateService.findByUserId(userId)
                .map(certificate ->  {
                    Status status = certificate.getStatus();
                    return status.isInReview();
                })
                .orElse(null);


        return new ProfileDTO(
                user.getPersonalInfo().getName(),
                user.getProfileImageUrl(),
                isOwner,
                inReview
        );
    }
}
