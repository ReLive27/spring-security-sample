package com.relive.authorization;

import org.springframework.lang.Nullable;

import java.util.Set;

/**
 * @author: ReLive
 * @date: 2023/5/6 19:01
 */
@FunctionalInterface
public interface TreePathProvider {

    @Nullable
    Set<String> providePathData();
}
