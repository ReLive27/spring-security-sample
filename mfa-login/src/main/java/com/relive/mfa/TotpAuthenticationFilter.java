package com.relive.mfa;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author: ReLive
 * @date: 2023/1/7 22:50
 */
public final class TotpAuthenticationFilter extends OncePerRequestFilter {
    private final AuthenticationManager authenticationManager;
    private final RequestMatcher requestMatcher;
    private AuthenticationConverter authenticationConverter;

    public TotpAuthenticationFilter(AuthenticationManager authenticationManager,
                                    RequestMatcher requestMatcher) {
        Assert.notNull(authenticationManager, "authenticationManager cannot be null");
        Assert.notNull(requestMatcher, "requestMatcher cannot be null");
        this.authenticationManager = authenticationManager;
        this.requestMatcher = requestMatcher;
        this.authenticationConverter = new TotpAuthenticationConverter();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (!this.requestMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Authentication authentication = this.authenticationConverter.convert(request);
            if (authentication != null) {
                Authentication authenticationResult = this.authenticationManager.authenticate(authentication);
                //TODO authenticationSuccessHandler
            }
            filterChain.doFilter(request, response);

        } catch (AuthenticationException e) {
            //TODO authenticationFailureHandler
        }
    }

    public void setAuthenticationConverter(AuthenticationConverter authenticationConverter) {
        this.authenticationConverter = authenticationConverter;
    }
}
