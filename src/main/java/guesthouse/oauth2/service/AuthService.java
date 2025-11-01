package guesthouse.oauth2.service;

import guesthouse.oauth2.domain.model.Oauth2Account;
import guesthouse.oauth2.domain.vo.Provider;
import guesthouse.oauth2.repository.Oauth2AccountRepository;
import guesthouse.user.domain.model.User;
import guesthouse.user.repository.UserRepository;
import guesthouse.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final TokenProcessor tokenProcessor;
    private final UserService userService;
    private final UserRepository userRepository;
    private final Oauth2AccountRepository oauth2AccountRepository;

    public String loginWithProvider(Long socialId, Provider provider) {
        Long userId = findUser(socialId, provider);
        return tokenProcessor.generateAccessToken(userId);
    }

    private Long findUser(Long socialId, Provider provider) {
        return oauth2AccountRepository.findBySocialId(socialId, provider)
                .map(Oauth2Account::getUserId)
                .orElseGet(() -> createUser(socialId, provider));
    }

    private Long createUser(Long socialId, Provider provider) {
        User savedUser = userService.createEmptyUser();

        Oauth2Account newOauth2Account = new Oauth2Account(savedUser.getId(), provider, socialId);
        oauth2AccountRepository.save(newOauth2Account);
        return savedUser.getId();
    }

}

