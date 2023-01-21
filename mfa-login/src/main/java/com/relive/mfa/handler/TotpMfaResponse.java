package com.relive.mfa.handler;

import lombok.Getter;

/**
 * @author: ReLive
 * @date: 2023/1/16 19:16
 */
@Getter
public class TotpMfaResponse {
    private String message;
    private String mfa;
    private String qrCode;
    private String token;
    private boolean authenticated;

    public TotpMfaResponse(String message, String mfa, String qrCode, String token, boolean authenticated) {
        this.message = message;
        this.mfa = mfa;
        this.qrCode = qrCode;
        this.token = token;
        this.authenticated = authenticated;
    }

    public static TotpMfaResponse unauthenticated(String message, String mfa, String qrCode) {
        return new TotpMfaResponse(message, mfa, qrCode, null, false);
    }

    public static TotpMfaResponse authenticated(String mfa, String token) {
        return new TotpMfaResponse("SUCCESS", mfa, null, token, true);
    }
}
