package com.relive27.mfa.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * @author: ReLive27
 * @date: 2023/1/8 21:32
 */
public class MfaAuthenticationException extends AuthenticationException {

    public MfaAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
