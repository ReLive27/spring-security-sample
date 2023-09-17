package com.relive27.captcha;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: ReLive27
 * @date: 2023/8/29 14:29
 */
public interface CaptchaAuthorizationResponseRepository<T extends CaptchaAuthorizationResponse> {

    T loadCaptchaAuthorizationResponse(HttpServletRequest request);

    void saveAuthorizationResponse(T authenticationResponse, HttpServletRequest request, HttpServletResponse response);

    T removeAuthorizationResponse(HttpServletRequest request, HttpServletResponse response);
}
