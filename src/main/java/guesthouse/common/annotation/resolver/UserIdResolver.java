package guesthouse.common.annotation.resolver;

import guesthouse.common.annotation.UserId;
import guesthouse.oauth2.exception.AuthenticationFailException;
import guesthouse.oauth2.service.TokenProcessor;
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
        String token = extractToken(header);

        return tokenProcessor.parseAccessToken(token);
    }


    private void validateTokenIsNull(String header) {
        if (header == null)
            throw new AuthenticationFailException();
    }

    private String extractToken(String header) {
        validateTokenIsNull(header);

        if (!header.startsWith(AUTH_TOKEN_HEADER)) {
            throw new AuthenticationFailException();
        }
        return header.substring(AUTH_TOKEN_HEADER.length());
    }
}
