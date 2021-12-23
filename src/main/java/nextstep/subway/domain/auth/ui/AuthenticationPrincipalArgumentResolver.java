package nextstep.subway.domain.auth.ui;

import nextstep.subway.domain.auth.application.AuthService;
import nextstep.subway.domain.auth.domain.AnonymousUser;
import nextstep.subway.domain.auth.domain.AuthenticationPrincipal;
import nextstep.subway.domain.auth.infrastructure.AuthorizationExtractor;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

public class AuthenticationPrincipalArgumentResolver implements HandlerMethodArgumentResolver {
    private final AuthService authService;

    public AuthenticationPrincipalArgumentResolver(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String credentials = AuthorizationExtractor.extract(webRequest.getNativeRequest(HttpServletRequest.class));
        if (isAnonymousUser(credentials)) {
            return new AnonymousUser();
        }
        return authService.findMemberByToken(credentials);
    }

    private boolean isAnonymousUser(final String credentials) {
        return !StringUtils.hasText(credentials) || credentials.equals("null");
    }
}