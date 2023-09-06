package com.relive27.mfa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.relive27.mfa.authentication.MfaAuthenticationToken;
import com.relive27.mfa.context.MfaAuthenticationTokenContextHolder;
import com.relive27.mfa.context.MfaTokenContext;
import com.relive27.mfa.exception.MfaAuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: ReLive27
 * @date: 2023/1/7 22:50
 */
public final class MfaAuthenticationFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AuthenticationManager authenticationManager;
    private final RequestMatcher requestMatcher;
    private AuthenticationConverter authenticationConverter;
    private AuthenticationSuccessHandler authenticationSuccessHandler = this::onAuthenticationSuccess;
    private AuthenticationFailureHandler authenticationFailureHandler = this::onAuthenticationFailure;


    public MfaAuthenticationFilter(AuthenticationManager authenticationManager,
                                   RequestMatcher requestMatcher) {
        Assert.notNull(authenticationManager, "authenticationManager cannot be null");
        Assert.notNull(requestMatcher, "requestMatcher cannot be null");
        this.authenticationManager = authenticationManager;
        this.requestMatcher = requestMatcher;
        this.authenticationConverter = new MfaAuthenticationConverter();
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
                this.authenticationSuccessHandler.onAuthenticationSuccess(request, response, authenticationResult);
            }
            filterChain.doFilter(request, response);

        } catch (AuthenticationException e) {
            this.authenticationFailureHandler.onAuthenticationFailure(request, response, e);
        } finally {
            MfaAuthenticationTokenContextHolder.resetMfaTokenContext();
        }
    }

    public void setAuthenticationConverter(AuthenticationConverter authenticationConverter) {
        this.authenticationConverter = authenticationConverter;
    }

    public void setAuthenticationSuccessHandler(AuthenticationSuccessHandler authenticationSuccessHandler) {
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    public void setAuthenticationFailureHandler(AuthenticationFailureHandler authenticationFailureHandler) {
        this.authenticationFailureHandler = authenticationFailureHandler;
    }

    private void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) {
        MfaAuthenticationToken mfaAuthenticationToken = (MfaAuthenticationToken) authentication;
        MfaTokenContext context = new MfaTokenContext(mfaAuthenticationToken.isMfa());
        MfaAuthenticationTokenContextHolder.setMfaTokenContext(context);
    }

    private void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                         AuthenticationException exception) throws IOException {
        Map<String, Object> responseClaims = new HashMap<>();
        responseClaims.put("code", HttpStatus.BAD_REQUEST.value());
        if (exception instanceof MfaAuthenticationException) {
            MfaAuthenticationException mfaAuthenticationException = (MfaAuthenticationException) exception;
            responseClaims.put("message", mfaAuthenticationException.getMessage());
        } else {
            responseClaims.put("message", "invalid code");
        }
        try (Writer writer = response.getWriter()) {
            writer.write(objectMapper.writeValueAsString(responseClaims));
        }
    }
}
