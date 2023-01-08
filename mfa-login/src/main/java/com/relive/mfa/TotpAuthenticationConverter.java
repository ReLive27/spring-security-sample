package com.relive.mfa;

import com.relive.mfa.authentication.TotpAuthenticationToken;
import com.relive.mfa.exception.TotpAuthenticationException;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author: ReLive
 * @date: 2023/1/8 21:11
 */
public class TotpAuthenticationConverter implements AuthenticationConverter {
    public static final String SPRING_SECURITY_MFA_TOTP_KEY = "code";
    private RequestMatcher requestMatcher = createLoginRequestMatcher();


    @Nullable
    @Override
    public Authentication convert(HttpServletRequest request) {

        String secret = this.obtainSecret(request);
        if (!StringUtils.hasText(secret)) {
            throw new TotpAuthenticationException("Totp secret parameter error", null);
        }

        String username = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            username = authentication.getName();
        } else {

            if (requestMatcher.matches(request)) {
                username = request.getParameter("username");
            }
        }

        if (StringUtils.hasText(username)) {
            return new TotpAuthenticationToken(username, secret);
        }

        return null;
    }

    @Nullable
    protected String obtainSecret(HttpServletRequest request) {
        return request.getParameter(SPRING_SECURITY_MFA_TOTP_KEY);
    }

    private static RequestMatcher createLoginRequestMatcher() {
        return new AntPathRequestMatcher("/login", "POST");
    }

    public void setRequestMatcher(RequestMatcher requestMatcher) {
        this.requestMatcher = requestMatcher;
    }
}