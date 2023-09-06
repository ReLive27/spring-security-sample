package com.relive27.mfa.jwt;

import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;

/**
 * @author: ReLive27
 * @date: 2023/1/13 19:17
 */
@FunctionalInterface
public interface TokenGenerator<T> {

    @Nullable
    T generate(Authentication authentication);
}
