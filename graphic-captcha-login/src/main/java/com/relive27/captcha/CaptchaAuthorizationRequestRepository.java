package com.relive27.captcha;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: ReLive27
 * @date: 2023/8/29 14:29
 */
public interface CaptchaAuthorizationRequestRepository<T extends CaptchaAuthorizationRequest> {

    T loadCaptchaAuthorizationRequest(HttpServletRequest request);

    void saveAuthorizationRequest(T authorizationRequest, HttpServletRequest request, HttpServletResponse response);

    T removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response);
}
