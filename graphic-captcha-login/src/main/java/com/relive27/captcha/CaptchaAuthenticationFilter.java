package com.relive27.captcha;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * @author: ReLive27
 * @date: 2023/8/23 22:59
 */
public class CaptchaAuthenticationFilter extends OncePerRequestFilter implements ApplicationEventPublisherAware {
    public static final String DEFAULT_LOGIN_REQUEST_BASE_URI = "/login";
    private final HttpMessageConverter<Object> errorHttpMessageConverter = new MappingJackson2HttpMessageConverter();

    private RequestMatcher requestMatcher;

    protected ApplicationEventPublisher eventPublisher;

    private CaptchaAuthorizationRequestResolver authorizationRequestResolver = new DefaultCaptchaAuthorizationRequestResolver();

    private CaptchaAuthorizationResponseRepository<CaptchaAuthorizationResponse> authorizationResponseRepository = new HttpSessionCaptchaAuthorizationResponseRepository();

    public CaptchaAuthenticationFilter() {
        this(DEFAULT_LOGIN_REQUEST_BASE_URI);
    }

    public CaptchaAuthenticationFilter(String defaultLoginRequestUri) {
        this(new AntPathRequestMatcher(defaultLoginRequestUri));
    }

    public CaptchaAuthenticationFilter(RequestMatcher requestMatcher) {
        Assert.notNull(requestMatcher, "RequestMatcher cannot be null");
        this.requestMatcher = requestMatcher;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!this.requestMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        CaptchaAuthorizationRequest authorizationRequest;
        try {
            authorizationRequest = this.authorizationRequestResolver.resolve(request);
        } catch (Exception e) {
            this.unsuccessfulAuthorization(request, response, new BadCaptchaException(new CaptchaAuthenticationExchange(null, null)));
            return;
        }
        CaptchaAuthorizationResponse authorizationResponse = this.authorizationResponseRepository.removeAuthorizationResponse(request, response);
        if (authorizationResponse == null ||
                !authorizationRequest.getCaptcha().equalsIgnoreCase(authorizationResponse.getCaptcha())) {
            this.unsuccessfulAuthorization(request, response, new BadCaptchaException(new CaptchaAuthenticationExchange(authorizationRequest, authorizationResponse)));
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private void unsuccessfulAuthorization(HttpServletRequest request, HttpServletResponse response,
                                           AuthenticationException ex) throws IOException {
        if (this.eventPublisher != null) {
            BadCaptchaException e = (BadCaptchaException) ex;
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(e.getAuthenticationExchange().getAuthorizationRequest() == null ?
                    "anonymousUser" : e.getAuthenticationExchange().getAuthorizationRequest().getUsername(), "");
            this.eventPublisher.publishEvent(new AuthenticationFailureBadCaptchaEvent(authenticationToken, ex));

        }
        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        this.errorHttpMessageConverter.write(Collections.singletonMap("message", ex.getMessage()), null, httpResponse);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void setAuthorizationRequestResolver(CaptchaAuthorizationRequestResolver authorizationRequestResolver) {
        this.authorizationRequestResolver = authorizationRequestResolver;
    }

    public void setAuthorizationResponseRepository(CaptchaAuthorizationResponseRepository<CaptchaAuthorizationResponse> authorizationResponseRepository) {
        this.authorizationResponseRepository = authorizationResponseRepository;
    }

    public void setRequestMatcher(RequestMatcher requestMatcher) {
        this.requestMatcher = requestMatcher;
    }
}
