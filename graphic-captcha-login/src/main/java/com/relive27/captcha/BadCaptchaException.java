package com.relive27.captcha;

import org.springframework.security.core.AuthenticationException;
import org.springframework.util.StringUtils;

/**
 * @author: ReLive27
 * @date: 2023/8/28 10:41
 */
public class BadCaptchaException extends AuthenticationException {

    public BadCaptchaException(String inputCaptcha, String expectedCaptcha) {
        this(!StringUtils.hasText(inputCaptcha) ? "验证码不能为空" :
                !StringUtils.hasText(expectedCaptcha) ? "验证码已过期" :
                        "验证码错误");
    }

    public BadCaptchaException(String msg) {
        super(msg);
    }

    public BadCaptchaException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
