package guesthouse.common.annotation.resolver;

import guesthouse.common.annotation.UserId;
import guesthouse.oauth2.exception.AuthenticationFailException;
import guesthouse.oauth2.service.TokenProcessor;
import guesthouse.user.exception.UserException;
import guesthouse.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class UserIdResolver implements HandlerMethodArgumentResolver {
    private static final String AUTH_TOKEN_HEADER = "Bearer ";

    private final TokenProcessor tokenProcessor;
    private final UserRepository userRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(UserId.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        String header = webRequest.getHeader(HttpHeaders.AUTHORIZATION);

        if (!hasToken(header)) {
            if (isUserIdRequired(parameter)) //required가 true인데 토큰이 없으면 예외
                throw new AuthenticationFailException();
            return null; //required가 false이면서 토큰이 없으면 null 반환
        }

        String token = extractToken(header);
        Long userId = tokenProcessor.parseAccessToken(token);
        userRepository.findById(userId)
                .orElseThrow(() -> new UserException());
        return userId;
    }

    private boolean isUserIdRequired(MethodParameter parameter) {
        UserId annotation = parameter.getParameterAnnotation(UserId.class);
        return annotation.required();
    }

    private Boolean hasToken(String header) {
        return header != null;
    }

    private String extractToken(String header) {
        if (!header.startsWith(AUTH_TOKEN_HEADER)) {
            throw new AuthenticationFailException();
        }
        return header.substring(AUTH_TOKEN_HEADER.length());
    }
}
