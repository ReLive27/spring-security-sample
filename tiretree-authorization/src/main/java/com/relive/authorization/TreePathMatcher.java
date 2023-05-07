package com.relive.authorization;

import java.util.Map;

/**
 * @author: ReLive
 * @date: 2023/5/5 20:35
 */
public interface TreePathMatcher {

    boolean matches(String path);

    Map<String, String> extractRole(String path);

    boolean buildTree();

    boolean rebuildTree();
}
