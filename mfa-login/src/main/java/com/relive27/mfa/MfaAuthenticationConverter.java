package com.relive27.mfa;

import com.relive27.mfa.authentication.MfaAuthenticationToken;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author: ReLive27
 * @date: 2023/1/8 21:11
 */
public class MfaAuthenticationConverter implements AuthenticationConverter {
    public static final String SPRING_SECURITY_MFA_PARAM_NAME = "code";
    private RequestMatcher requestMatcher = createLoginRequestMatcher();


    @Nullable
    @Override
    public Authentication convert(HttpServletRequest request) {

        String username = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            username = authentication.getName();
            authentication.setAuthenticated(false);
        } else {

            if (requestMatcher.matches(request)) {
                username = request.getParameter("username");
            }
        }

        if (!StringUtils.hasText(username)) {
            return null;
        }

        String secret = this.obtainSecret(request);
        if (StringUtils.hasText(secret)) {
            if (authentication != null) {
                authentication.setAuthenticated(true);
            }
            return new MfaAuthenticationToken(username, secret);
        }

        return null;
    }

    @Nullable
    protected String obtainSecret(HttpServletRequest request) {
        return request.getParameter(SPRING_SECURITY_MFA_PARAM_NAME);
    }

    private static RequestMatcher createLoginRequestMatcher() {
        return new AntPathRequestMatcher("/login", "POST");
    }

    public void setRequestMatcher(RequestMatcher requestMatcher) {
        this.requestMatcher = requestMatcher;
    }
}
