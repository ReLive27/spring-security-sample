package com.relive27.captcha;

import org.springframework.security.core.AuthenticationException;

/**
 * @author: ReLive27
 * @date: 2023/8/28 10:41
 */
public class BadCaptchaException extends AuthenticationException {

    private CaptchaAuthenticationExchange authenticationExchange;

    public BadCaptchaException(CaptchaAuthenticationExchange authenticationExchange) {
        this(authenticationExchange.getAuthorizationRequest() == null ? "验证码不能为空" :
                authenticationExchange.getAuthorizationResponse() == null ? "验证码已过期" :
                        "验证码错误");
        this.authenticationExchange = authenticationExchange;
    }

    public BadCaptchaException(String msg) {
        super(msg);
    }

    public BadCaptchaException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public CaptchaAuthenticationExchange getAuthenticationExchange() {
        return authenticationExchange;
    }
}
