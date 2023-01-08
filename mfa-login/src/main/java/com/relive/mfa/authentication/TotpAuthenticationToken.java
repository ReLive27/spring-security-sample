package com.relive.mfa.authentication;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * @author: ReLive
 * @date: 2023/1/8 21:13
 */
public class TotpAuthenticationToken extends AbstractAuthenticationToken {
    private final Object principal;
    private final String credentials;
    private boolean mfa;

    public TotpAuthenticationToken(Object principal, String credentials) {
        super(null);
        this.principal = principal;
        this.credentials = credentials;
        this.setAuthenticated(false);
    }

    public TotpAuthenticationToken(Object principal, String credentials, boolean mfa) {
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
