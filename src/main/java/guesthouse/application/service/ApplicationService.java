package guesthouse.application.service;

import guesthouse.application.domain.model.Application;
import guesthouse.application.domain.vo.Mbti;
import guesthouse.application.dto.MyApplicationDTO;
import guesthouse.application.dto.requset.ApplicationSaveRequest;
import guesthouse.application.exception.ApplicationErrorCode;
import guesthouse.application.exception.ApplicationException;
import guesthouse.application.mapper.ApplicationMapper;
import guesthouse.application.repository.ApplicationRepository;
import guesthouse.user.domain.model.User;
import guesthouse.user.domain.vo.Gender;
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
                request.imageUrl(),
                userId
        );

        return applicationRepository.findByUserId(userId)
                .map(existing -> {
                    existing.update(
                            request.availableStartDate(),
                            ApplicationMapper.toDayOfWeekSet(request.availableDayOfWeek()),
                            request.selfIntroduction(),
                            Mbti.fromValue(request.mbti()),
                            ApplicationMapper.toStyleSet(request.style()),
                            request.instagramId(),
                            request.imageUrl()
                    );
                    return existing.getId();
                })
                .orElseGet(() -> {
                    User user = userService.findById(userId);
                    Application application = ApplicationMapper.toEntity(request, user);
                    return applicationRepository.save(application).getId();
                });
    }

    public Application getApplication(Long userId) {
        return applicationRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Boolean hasApplication(Long userId) {
        return applicationRepository.existsByUserId(userId);
    }


    @Transactional(readOnly = true)
    public MyApplicationDTO getMyApplication(Long userId) {
        return MyApplicationDTO.from(getApplication(userId));
    }
}
