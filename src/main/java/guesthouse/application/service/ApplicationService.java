package guesthouse.application.service;

import guesthouse.application.domain.model.Application;
import guesthouse.user.domain.vo.Gender;
import guesthouse.application.dto.requset.ApplicationSaveRequest;
import guesthouse.application.mapper.ApplicationMapper;
import guesthouse.application.repository.ApplicationRepository;
import guesthouse.user.domain.model.User;
import guesthouse.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final UserService userService;
    private final ApplicationRepository applicationRepository;

    @Transactional
    public Long save(ApplicationSaveRequest request, Long userId) {
        userService.updatePersonalInfo(
                request.name(),
                request.phoneNumber(),
                request.birthDate(),
                Gender.fromValue(request.gender()),
                userId);

        User user = userService.findById(userId);
        Application application = ApplicationMapper.toEntity(request, user);

        return applicationRepository.save(application).getId();
    }

    @Transactional(readOnly = true)
    public Boolean hasApplication(Long userId) {
        return applicationRepository.existsById(userId);
    }
}
