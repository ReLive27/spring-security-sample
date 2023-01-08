package com.relive.mfa.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * @author: ReLive
 * @date: 2023/1/8 21:32
 */
public class TotpAuthenticationException extends AuthenticationException {

    public TotpAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
