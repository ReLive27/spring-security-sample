package com.relive27.captcha;

import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * @author: ReLive27
 * @date: 2023/8/28 11:03
 */
public class AuthenticationFailureBadCaptchaEvent extends AbstractAuthenticationFailureEvent {

    public AuthenticationFailureBadCaptchaEvent(Authentication authentication, AuthenticationException exception) {
        super(authentication, exception);
    }
}
