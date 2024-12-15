package com.relive27.authorization;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 基于角色的权限管理器
 *
 * @author: ReLive27
 * @date: 2023/5/5 20:21
 */
public class RoleAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    public static final String AUTHORIZE_KEY = "role";

    /**
     * 检查当前请求的授权状态
     *
     * @param authentication 提供当前用户认证信息的 Supplier
     * @param matchResult    包含当前请求的上下文信息
     * @return 授权决策结果
     */
    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext matchResult) {
        // 判断用户是否具备授权
        boolean granted = this.isGranted(authentication.get(), matchResult);
        // 返回授权决策
        return new AuthorizationDecision(granted);
    }

    /**
     * 判断是否授权通过
     *
     * @param authentication 当前用户认证信息
     * @param matchResult    包含当前请求的上下文信息
     * @return true 表示授权通过，false 表示授权失败
     */
    private boolean isGranted(Authentication authentication, RequestAuthorizationContext matchResult) {
        // 用户已认证且拥有所需权限才返回 true
        return authentication != null
                && authentication.isAuthenticated()
                && this.isAuthorized(authentication, matchResult);
    }

    /**
     * 判断用户是否具备所需角色权限
     *
     * @param authentication 当前用户认证信息
     * @param matchResult    包含当前请求的上下文信息
     * @return true 表示用户具备所需权限，false 表示权限不足
     */
    private boolean isAuthorized(Authentication authentication, RequestAuthorizationContext matchResult) {
        // 从请求上下文中获取角色授权信息
        String authorize = matchResult.getVariables().get(AUTHORIZE_KEY);

        // 将逗号分隔的角色列表转为 Set 集合
        Set<String> requiredAuthorities = StringUtils.commaDelimitedListToSet(authorize);

        // 遍历用户的权限列表，检查是否匹配所需权限
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (requiredAuthorities.contains(authority.getAuthority())) {
                return true; // 用户具备至少一个所需权限
            }
        }
        return false; // 用户不具备所需权限
    }
}
