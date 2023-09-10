package com.relive27.captcha;

import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author: ReLive27
 * @date: 2023/9/1 10:17
 */
public final class HttpSessionCaptchaAuthorizationRequestRepository implements CaptchaAuthorizationRequestRepository<CaptchaAuthorizationRequest> {
    private static final String DEFAULT_AUTHORIZATION_REQUEST_ATTR_NAME = HttpSessionCaptchaAuthorizationRequestRepository.class
            .getName() + ".AUTHORIZATION_REQUEST";

    private final String sessionAttributeName = DEFAULT_AUTHORIZATION_REQUEST_ATTR_NAME;


    @Override
    public CaptchaAuthorizationRequest loadCaptchaAuthorizationRequest(HttpServletRequest request) {
        Assert.notNull(request, "request cannot be null");
        CaptchaAuthorizationRequest authorizationRequest = this.getAuthorizationRequests(request);
        return authorizationRequest;
    }

    @Override
    public void saveAuthorizationRequest(CaptchaAuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        Assert.notNull(request, "request cannot be null");
        Assert.notNull(response, "response cannot be null");
        if (authorizationRequest == null) {
            this.removeAuthorizationRequest(request, response);
            return;
        }
        request.getSession().setAttribute(this.sessionAttributeName, authorizationRequest);

    }

    @Override
    public CaptchaAuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        Assert.notNull(request, "request cannot be null");

        CaptchaAuthorizationRequest authorizationRequest = this.getAuthorizationRequests(request);
        if (authorizationRequest != null) {
            request.getSession().removeAttribute(this.sessionAttributeName);
        }
        return authorizationRequest;
    }

    private CaptchaAuthorizationRequest getAuthorizationRequests(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (CaptchaAuthorizationRequest) session.getAttribute(this.sessionAttributeName) : null;
    }
}
