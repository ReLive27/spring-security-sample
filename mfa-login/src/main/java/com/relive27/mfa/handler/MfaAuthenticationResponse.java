package com.relive27.mfa.handler;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * @author: ReLive27
 * @date: 2023/1/16 19:16
 */
@Getter
public class MfaAuthenticationResponse {
    private String message;
    private String mfa;
    private String qrCode;
    private String token;
    private int responseCode;
    private boolean authenticated;

    public MfaAuthenticationResponse(String message, String mfa, String qrCode, String token, int responseCode, boolean authenticated) {
        this.message = message;
        this.mfa = mfa;
        this.qrCode = qrCode;
        this.token = token;
        this.responseCode = responseCode;
        this.authenticated = authenticated;
    }

    public static MfaAuthenticationResponse unauthenticated(String message, String mfa, HttpStatus status, String qrCode) {
        return new MfaAuthenticationResponse(message, mfa, qrCode, null, status.value(), false);
    }

    public static MfaAuthenticationResponse authenticated(String mfa, String token) {
        return new MfaAuthenticationResponse("SUCCESS", mfa, null, token, HttpStatus.OK.value(), true);
    }
}
