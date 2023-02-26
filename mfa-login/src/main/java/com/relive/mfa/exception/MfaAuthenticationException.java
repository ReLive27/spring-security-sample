package com.relive.mfa.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * @author: ReLive
 * @date: 2023/1/8 21:32
 */
public class MfaAuthenticationException extends AuthenticationException {

    public MfaAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
