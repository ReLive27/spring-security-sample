package com.relive27.captcha;

import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author: ReLive27
 * @date: 2023/9/1 10:17
 */
public final class HttpSessionCaptchaAuthorizationResponseRepository implements CaptchaAuthorizationResponseRepository<CaptchaAuthorizationResponse> {
    private static final String DEFAULT_AUTHORIZATION_REQUEST_ATTR_NAME = HttpSessionCaptchaAuthorizationResponseRepository.class
            .getName() + ".AUTHORIZATION_REQUEST";

    private final String sessionAttributeName = DEFAULT_AUTHORIZATION_REQUEST_ATTR_NAME;


    @Override
    public CaptchaAuthorizationResponse loadCaptchaAuthorizationResponse(HttpServletRequest request) {
        Assert.notNull(request, "request cannot be null");
        CaptchaAuthorizationResponse authorizationResponse = this.getAuthorizationResponses(request);
        return authorizationResponse;
    }

    @Override
    public void saveAuthorizationResponse(CaptchaAuthorizationResponse authorizationResponse, HttpServletRequest request, HttpServletResponse response) {
        Assert.notNull(request, "request cannot be null");
        Assert.notNull(response, "response cannot be null");
        if (authorizationResponse == null) {
            this.removeAuthorizationResponse(request, response);
            return;
        }
        request.getSession().setAttribute(this.sessionAttributeName, authorizationResponse);

    }

    @Override
    public CaptchaAuthorizationResponse removeAuthorizationResponse(HttpServletRequest request, HttpServletResponse response) {
        Assert.notNull(request, "request cannot be null");

        CaptchaAuthorizationResponse authorizationResponse = this.getAuthorizationResponses(request);
        if (authorizationResponse != null) {
            request.getSession().removeAttribute(this.sessionAttributeName);
        }
        return authorizationResponse;
    }

    private CaptchaAuthorizationResponse getAuthorizationResponses(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (CaptchaAuthorizationResponse) session.getAttribute(this.sessionAttributeName) : null;
    }
}
