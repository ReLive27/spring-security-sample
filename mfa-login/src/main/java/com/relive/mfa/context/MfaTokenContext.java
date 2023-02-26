package com.relive.mfa.context;

/**
 * @author: ReLive
 * @date: 2023/1/7 23:20
 */
public final class MfaTokenContext {
    private final boolean mfa;

    public MfaTokenContext(boolean mfa) {
        this.mfa = mfa;
    }

    public boolean isMfa() {
        return this.mfa;
    }
}
