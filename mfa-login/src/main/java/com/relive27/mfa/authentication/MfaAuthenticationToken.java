package com.relive27.mfa.authentication;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * @author: ReLive27
 * @date: 2023/1/8 21:13
 */
public class MfaAuthenticationToken extends AbstractAuthenticationToken {
    private final Object principal;
    private final String credentials;
    private boolean mfa;

    public MfaAuthenticationToken(Object principal, String credentials) {
        super(null);
        this.principal = principal;
        this.credentials = credentials;
        this.setAuthenticated(false);
    }

    public MfaAuthenticationToken(Object principal, String credentials, boolean mfa) {
        super(null);
        this.principal = principal;
        this.credentials = credentials;
        this.mfa = mfa;
    }

    @Override
    public String getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    public boolean isMfa() {
        return mfa;
    }
}
