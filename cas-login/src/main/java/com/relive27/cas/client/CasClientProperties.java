package com.relive27.cas.client;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * 用于配置单个CAS 客户端注册的类。
 *
 * @author: ReLive27
 * @date: 2022/12/1 21:19
 */
@Data
@ConfigurationProperties(prefix = "spring.security.cas.client")
public class CasClientProperties implements InitializingBean {

    /**
     * CAS 服务器的基本 URL（如：https://cas.example.com）。
     * <p>
     * 该字段表示与 CAS 服务器交互时使用的基本 URL。
     * </p>
     */
    private String casServerUrl;

    /**
     * CAS 服务器的登录 URL（如：https://cas.example.com/login）。
     * <p>
     * 该 URL 用于 CAS 客户端重定向用户到登录页面。
     * </p>
     */
    private String casServerLoginUrl;

    /**
     * CAS 服务器的登出 URL（如：https://cas.example.com/logout）。
     */
    private String casServerLogoutUrl;

    /**
     * 注销回掉路径（如：/logoutCallback）
     */
    private String logoutCallbackPath;

    /**
     * 服务URL
     */
    private String service;

    /**
     * 是否认证所有 artifacts，默认为 false。
     * <p>
     * 如果为 true，则 CAS 客户端将对所有 artifact（例如 ticket）进行认证。
     * </p>
     */
    private boolean authenticateAllArtifacts = false;

    /**
     * 是否发送 renew 参数，默认为 false。
     * <p>
     * 如果为 true，则在认证请求中会附带 renew=true 参数，表示强制要求重新认证。
     * </p>
     */
    private boolean sendRenew = false;

    /**
     * 认证请求中的 artifact 参数名称，默认为 "ticket"。
     * <p>
     * 该参数用于传递 CAS 认证凭证，默认是 ticket。
     * </p>
     */
    private String artifactParameter = "ticket";

    /**
     * 认证请求中的 service 参数名称，默认为 "service"。
     * <p>
     * 该参数用于标识请求服务，默认是 service。
     * </p>
     */
    private String serviceParameter = "service";


    /**
     * 初始化方法，在设置完所有属性后调用该方法。
     * <p>
     * 该方法会调用 validate() 方法来验证 CAS 客户端注册信息。
     * </p>
     */
    public void afterPropertiesSet() {
        this.validate();
    }


    public void validate() {
        if (!StringUtils.hasText(getCasServerUrl())) {
            throw new IllegalStateException(String.format("Cas server url  must not be empty."));
        }
        if (!StringUtils.hasText(getService())) {
            throw new IllegalStateException(String.format("Service of registration '%s' must not be empty."));
        }
        // 如果没有提供 casServerLoginUrl，则根据 casServerUrl 自动生成
        if (!StringUtils.hasText(getCasServerLoginUrl())) {
            this.casServerLoginUrl = getCasServerUrl().endsWith("/") ? getCasServerUrl().substring(0, getCasServerUrl().lastIndexOf("/")) + "/login" :
                    getCasServerUrl() + "/login";
        }
        // 如果没有提供 casServerLogoutUrl，则根据 casServerUrl 自动生成
        if (!StringUtils.hasText(getCasServerLogoutUrl())) {
            this.casServerLogoutUrl = getCasServerUrl().endsWith("/") ? getCasServerUrl().substring(0, getCasServerUrl().lastIndexOf("/")) + "/logout" :
                    getCasServerUrl() + "/logout";
        }
    }

}

