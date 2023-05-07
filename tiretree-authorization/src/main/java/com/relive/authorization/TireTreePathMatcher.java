package com.relive.authorization;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.relive.authorization.TireTreeAuthorizationManager.AUTHORIZE_KEY;

/**
 * @author: ReLive
 * @date: 2023/5/5 20:44
 */
public class TireTreePathMatcher implements TreePathMatcher {
    private final TireTree tireTree;
    private final TreePathProvider provider;

    public TireTreePathMatcher(TreePathProvider provider) {
        Assert.notNull(provider, "treePathProvider cannot be null");
        this.provider = provider;
        this.tireTree = new TireTree();
    }

    @Override
    public boolean matches(String path) {
        return StringUtils.hasText(this.tireTree.searchPath(path));
    }

    @Override
    public Map<String, String> extractRole(String path) {
        String extractRole = this.tireTree.searchPath(path);
        return StringUtils.hasText(extractRole) ?
                Collections.singletonMap(AUTHORIZE_KEY, extractRole) : Collections.emptyMap();
    }

    @Override
    public boolean buildTree() {
        Set<String> resources = this.provider.providePathData();
        if (!CollectionUtils.isEmpty(resources)) {
            this.tireTree.buildTree(resources);
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    @Override
    public boolean rebuildTree() {
        Set<String> resources = this.provider.providePathData();
        if (!CollectionUtils.isEmpty(resources)) {
            this.tireTree.rebuildTree(resources);
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }
}
