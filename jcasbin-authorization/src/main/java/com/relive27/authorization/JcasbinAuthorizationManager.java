package com.relive27.authorization;

import lombok.extern.slf4j.Slf4j;
import org.casbin.jcasbin.main.Enforcer;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * 自定义基于 JCasbin 的权限管理器。
 * 该类实现了 Spring Security 的 AuthorizationManager 接口，
 * 使用 JCasbin 的策略模型对用户请求进行权限验证
 *
 * @author: ReLive27
 * @date: 2024/12/29 21:33
 */
@Slf4j
public class JcasbinAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    // Casbin 的权限执行器，负责加载和执行策略
    private final Enforcer enforcer;

    // URL 路径Helper，用于从 HttpServletRequest 提取路径
    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    private volatile boolean lazyInit = true;

    // 用于保证策略加载线程安全的锁
    private final Object lock = new Object();

    /**
     * 构造函数，初始化权限执行器。
     *
     * @param enforcer Casbin 执行器，不可为空
     */
    public JcasbinAuthorizationManager(Enforcer enforcer) {
        Assert.notNull(enforcer, "enforcer 不能为空");
        this.enforcer = enforcer;
    }

    /**
     * 核心方法，用于对请求进行权限验证。
     *
     * @param authentication 用户认证信息提供者
     * @param context        请求上下文
     * @return 返回授权决策对象，包含是否允许访问的信息
     */
    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        boolean granted = isGranted(authentication.get(), context.getRequest());
        return new AuthorizationDecision(granted);
    }

    /**
     * 检查用户是否被授权访问请求资源。
     *
     * @param authentication 用户认证信息
     * @param request        HTTP 请求对象
     * @return 如果被授权返回 true，否则返回 false
     */
    private boolean isGranted(Authentication authentication, HttpServletRequest request) {
        return authentication != null
                && authentication.isAuthenticated()
                && isAuthorized(authentication, request);
    }

    /**
     * 实际权限检查逻辑，基于 JCasbin 策略模型。
     *
     * @param authentication 用户认证信息
     * @param request        HTTP 请求对象
     * @return 如果策略允许访问返回 true，否则返回 false
     */
    private boolean isAuthorized(Authentication authentication, HttpServletRequest request) {
        // 双重检查锁确保策略仅加载一次，提升性能
        if (this.lazyInit) {
            if (CollectionUtils.isEmpty(enforcer.getPolicy())) {
                synchronized (lock) {
                    if (CollectionUtils.isEmpty(enforcer.getPolicy())) {
                        try {
                            enforcer.loadPolicy(); // 加载策略
                            if (!CollectionUtils.isEmpty(enforcer.getPolicy())) {
                                this.lazyInit = false;
                            }
                        } catch (Exception e) {
                            log.error("加载策略失败，请检查配置", e);
                            return false;
                        }
                    }
                }
            }
        }

        // 获取 HTTP 方法和路径信息
        String method = request.getMethod().toUpperCase(Locale.ROOT);
        String path = urlPathHelper.getPathWithinApplication(request);

        try {
            // 使用 Casbin 执行权限验证
            return enforcer.enforce(authentication.getName(), path, method);
        } catch (Exception e) {
            log.error("权限验证失败，请检查策略和请求路径: {},{}", path, method, e);
            return false;
        }
    }
}
