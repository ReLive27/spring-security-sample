package com.relive27.authorization;

import com.relive27.authorization.adapter.Adapter;
import com.relive27.authorization.watcher.Watcher;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author: ReLive27
 * @date: 2024/12/15 20:42
 */
public class SecurityUtils {

    /**
     * 获取一个角色请求匹配器，依赖于 PolicyEnforcer 和其他组件（如 Adapter、Watcher）的配置。
     *
     * @param httpSecurity HttpSecurity 对象，用于访问 Spring Security 的配置。
     * @return 返回一个角色请求匹配器（RequestMatcher），用于根据策略检查请求。
     */
    public static RequestMatcher getRoleRequestMatcher(HttpSecurity httpSecurity) {
        // 尝试从 HttpSecurity 获取 PolicyEnforcer 实例
        PolicyEnforcer policyEnforcer = httpSecurity.getSharedObject(PolicyEnforcer.class);

        // 如果没有找到，则尝试从 Spring 容器中获取 PolicyEnforcer
        if (policyEnforcer == null) {
            policyEnforcer = getOptionalBean(httpSecurity, PolicyEnforcer.class);

            // 如果容器中也没有，则创建一个新的 RolePolicyEnforcer 实例
            if (policyEnforcer == null) {
                policyEnforcer = new RolePolicyEnforcer();

                // 尝试获取 Adapter 并设置到 PolicyEnforcer
                Adapter adapter = getAdapter(httpSecurity);
                if (adapter != null) {
                    ((RolePolicyEnforcer) policyEnforcer).setAdapter(adapter);
                }

                // 尝试获取 Watcher 并设置到 PolicyEnforcer
                Watcher watcher = getWatcher(httpSecurity);
                if (watcher != null) {
                    ((RolePolicyEnforcer) policyEnforcer).setWatcher(watcher);
                }
            }
        }

        // 返回一个 RoleRequestMatcher，使用上述构建的 PolicyEnforcer
        return new RoleRequestMatcher(policyEnforcer);
    }

    /**
     * 获取 HttpSecurity 中配置的 Adapter。
     *
     * @param httpSecurity HttpSecurity 对象。
     * @return 返回 Adapter 实例，如果没有则返回 null。
     */
    private static Adapter getAdapter(HttpSecurity httpSecurity) {
        return getOptionalBean(httpSecurity, Adapter.class);
    }

    /**
     * 获取 HttpSecurity 中配置的 Watcher。
     *
     * @param httpSecurity HttpSecurity 对象。
     * @return 返回 Watcher 实例，如果没有则返回 null。
     */
    private static Watcher getWatcher(HttpSecurity httpSecurity) {
        return getOptionalBean(httpSecurity, Watcher.class);
    }

    /**
     * 从 Spring 容器中获取指定类型的 Bean。
     * 如果有多个同类型的 Bean，将抛出 NoUniqueBeanDefinitionException。
     *
     * @param httpSecurity HttpSecurity 对象。
     * @param type         要获取的 Bean 类型。
     * @param <T>          Bean 的类型。
     * @return 返回匹配的 Bean 实例，如果没有匹配的 Bean，则返回 null。
     * @throws NoUniqueBeanDefinitionException 如果找到多个同类型的 Bean，将抛出此异常。
     */
    static <T> T getOptionalBean(HttpSecurity httpSecurity, Class<T> type) {
        // 获取 Spring 容器中所有匹配的 Bean
        Map<String, T> beansMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(httpSecurity.getSharedObject(ApplicationContext.class), type);

        // 如果有多个匹配的 Bean，抛出异常
        if (beansMap.size() > 1) {
            throw new NoUniqueBeanDefinitionException(type, beansMap.size(),
                    "Expected single matching bean of type '" + type.getName() + "' but found " + beansMap.size() + ": " + StringUtils.collectionToCommaDelimitedString(beansMap.keySet()));
        } else {
            // 如果有找到 Bean，则返回唯一的一个，否则返回 null
            return !beansMap.isEmpty() ? beansMap.values().iterator().next() : null;
        }
    }
}
