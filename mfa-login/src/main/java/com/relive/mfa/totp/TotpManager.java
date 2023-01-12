package com.relive.mfa.totp;

/**
 * @author: ReLive
 * @date: 2023/1/12 19:27
 */
public interface TotpManager {

    String generateSecret();

    boolean verifyCode(String secret, String code);

    String getUriForImage(String label, String secret, String issuer) throws Exception;

}
