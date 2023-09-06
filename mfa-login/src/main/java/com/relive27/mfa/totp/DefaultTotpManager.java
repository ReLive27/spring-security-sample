package com.relive27.mfa.totp;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

/**
 * @author: ReLive27
 * @date: 2023/1/12 19:40
 */
public class DefaultTotpManager implements MfaAuthenticationManager {
    private static final int DEFAULT_SECRET_LENGTH = 64;
    private static final int DEFAULT_CODE_LENGTH = 6;
    private static final int DEFAULT_TIME_PERIOD = 30;
    private final QrDataFactory qrDataFactory;
    private final QrGenerator qrGenerator;
    private final SecretGenerator secretGenerator;
    private final CodeVerifier verifier;

    public DefaultTotpManager() {
        this.qrDataFactory = new QrDataFactory(HashingAlgorithm.SHA256, DEFAULT_CODE_LENGTH, DEFAULT_TIME_PERIOD);
        this.qrGenerator = new ZxingPngQrGenerator();
        this.secretGenerator = new DefaultSecretGenerator(DEFAULT_SECRET_LENGTH);
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA256, DEFAULT_CODE_LENGTH);
        this.verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    }

    @Override
    public String generateSecret() {
        return this.secretGenerator.generate();
    }

    @Override
    public boolean validCode(String secret, String code) {
        return this.verifier.isValidCode(secret, code);
    }

    @Override
    public String getUriForImage(String label, String secret, String issuer) throws QrGenerationException {
        QrData data = qrDataFactory.newBuilder()
                .label(label)
                .secret(secret)
                .issuer(issuer)
                .build();

        // Generate the QR code image data as a base64 string which
        // can be used in an <img> tag:
        String qrCodeImage = getDataUriForImage(
                qrGenerator.generate(data),
                qrGenerator.getImageMimeType()
        );
        return qrCodeImage;
    }
}
