package com.relive27.mfa.totp;

/**
 * @author: ReLive27
 * @date: 2023/1/12 19:27
 */
public interface MfaAuthenticationManager {

    String generateSecret();

    boolean validCode(String secret, String code);

    String getUriForImage(String label, String secret, String issuer) throws Exception;

}
