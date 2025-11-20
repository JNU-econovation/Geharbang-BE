package guesthouse.user.service;

import guesthouse.user.domain.model.User;
import guesthouse.user.domain.model.UserImage;
import guesthouse.user.domain.vo.Gender;
import guesthouse.user.exception.UserException;
import guesthouse.user.repository.UserImageRepository;
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
    private final UserImageRepository userImageRepository;

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(UserException::new);
    }

    @Transactional(readOnly = true)
    public Optional<UserImage> findImageByUserId(Long userId) {
        return userImageRepository.findByUserId(userId);
    }

    @Transactional
    public User createEmptyUser() {
        return userRepository.save(new User());
    }

    public void updatePersonalInfo(String name,
                                   String phoneNumber,
                                   LocalDate birthDate,
                                   Gender gender,
                                   Long userId) {
        User user = findById(userId);
        user.updatePersonalInfo(name, phoneNumber, birthDate, gender);
    }
}
