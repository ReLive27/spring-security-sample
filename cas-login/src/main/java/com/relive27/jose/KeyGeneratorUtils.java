package com.relive27.jose;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

/**
 * 工具类，用于生成 RSA 密钥对。
 * <p>
 * 该类包含静态方法来生成 RSA 密钥对。密钥对使用 2048 位密钥长度，并且该类的构造函数是私有的，避免实例化该类。
 * </p>
 *
 * @author: ReLive27
 * @date: 2025/2/5 22:20
 */
final class KeyGeneratorUtils {

    private KeyGeneratorUtils() {
    }

    /**
     * 生成 RSA 密钥对。
     * <p>
     * 使用 RSA 算法生成一个密钥对，密钥长度为 2048 位。此方法抛出异常时会封装为 {@link IllegalStateException}。
     * </p>
     *
     * @return 生成的 RSA 密钥对。
     * @throws IllegalStateException 如果生成密钥对过程中发生错误。
     */
    static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }
}

