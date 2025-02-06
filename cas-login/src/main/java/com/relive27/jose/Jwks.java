package com.relive27.jose;

import com.nimbusds.jose.jwk.RSAKey;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

/**
 * 用于生成 RSA 密钥对的 JWKS (JSON Web Key Set) 工具类。
 * <p>
 * 该类提供了生成 RSA 密钥对的方法，并返回一个包含公钥和私钥的 {@link RSAKey} 对象。
 * 生成的 RSA 密钥对将用于创建 JSON Web Key (JWKS)，并且每个密钥对都会分配一个唯一的 keyID。
 * </p>
 *
 * @author: ReLive27
 * @date: 2025/2/5 22:22
 */
public final class Jwks {

    private Jwks() {
    }

    /**
     * 生成一个 RSA 密钥对，并构建一个包含该密钥对的 {@link RSAKey} 对象。
     * <p>
     * 该方法会调用 {@link KeyGeneratorUtils} 生成 RSA 密钥对，并使用公钥和私钥创建一个 {@link RSAKey} 对象。
     * 生成的密钥会分配一个唯一的 keyID，以便标识该密钥。
     * </p>
     *
     * @return 生成的 {@link RSAKey} 对象，包含 RSA 公钥、私钥和一个唯一的 keyID。
     */
    public static RSAKey generateRsa() {
        KeyPair keyPair = KeyGeneratorUtils.generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString()) // 为密钥分配唯一的 keyID
                .build();
    }
}
