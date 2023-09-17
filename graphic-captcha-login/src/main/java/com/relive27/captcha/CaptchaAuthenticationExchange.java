package com.relive27.captcha;

/**
 * @author: ReLive27
 * @date: 2023/9/13 10:15ø
 */
public class CaptchaAuthenticationExchange {
    private final CaptchaAuthorizationRequest authorizationRequest;
    private final CaptchaAuthorizationResponse authorizationResponse;

    public CaptchaAuthenticationExchange(CaptchaAuthorizationRequest authorizationRequest,
                                         CaptchaAuthorizationResponse authorizationResponse) {
        this.authorizationRequest = authorizationRequest;
        this.authorizationResponse = authorizationResponse;
    }

    public CaptchaAuthorizationRequest getAuthorizationRequest() {
        return authorizationRequest;
    }

    public CaptchaAuthorizationResponse getAuthorizationResponse() {
        return authorizationResponse;
    }
}
