package com.relive.mfa.handler;

import lombok.Getter;
import org.springframework.http.HttpStatus;

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
    private int responseCode;
    private boolean authenticated;

    public TotpMfaResponse(String message, String mfa, String qrCode, String token, int responseCode, boolean authenticated) {
        this.message = message;
        this.mfa = mfa;
        this.qrCode = qrCode;
        this.token = token;
        this.responseCode = responseCode;
        this.authenticated = authenticated;
    }

    public static TotpMfaResponse unauthenticated(String message, String mfa, HttpStatus status, String qrCode) {
        return new TotpMfaResponse(message, mfa, qrCode, null, status.value(), false);
    }

    public static TotpMfaResponse authenticated(String mfa, String token) {
        return new TotpMfaResponse("SUCCESS", mfa, null, token, HttpStatus.OK.value(), true);
    }
}
