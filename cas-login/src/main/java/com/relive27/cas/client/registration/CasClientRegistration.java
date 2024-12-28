package com.relive27.cas.client.registration;

import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * 表示一个 CAS 客户端注册的配置类。
 * <p>
 * {@link CasClientRegistration} 用于配置与 CAS（Central Authentication Service）服务器的连接信息。
 * 该类包含了与 CAS 服务器通信所需的各项参数，如CAS 服务器 URL、服务 URL、认证相关的参数等。
 * </p>
 *
 * @author: ReLive27
 * @date: 2024/5/6 20:56
 */
public class CasClientRegistration {
    /**
     * CAS 服务器的 URL 地址。
     */
    private String casServerUrl;

    /**
     * CAS 服务器登录页面的 URL 地址。
     */
    private String casServerLoginUrl;

    /**
     * CAS 服务器登出 URL 地址
     */
    private String casServerLogoutUrl;

    /**
     * 注销回掉路径（如：/logoutCallback）
     */
    private String logoutCallbackPath;

    /**
     * 服务的 URL。
     */
    private String service;

    /**
     * 是否发送 renew 参数，通常用于刷新认证票据。
     */
    private boolean sendRenew;

    /**
     * 是否认证所有的 artifacts。
     */
    private boolean authenticateAllArtifacts;

    /**
     * 用于认证请求的 artifact 参数名称。
     */
    private String artifactParameter;

    /**
     * 服务参数的名称。
     */
    private String serviceParameter;

    public String getCasServerUrl() {
        return casServerUrl;
    }

    public String getCasServerLoginUrl() {
        return casServerLoginUrl;
    }

    public String getService() {
        return service;
    }

    public boolean isSendRenew() {
        return sendRenew;
    }

    public boolean isAuthenticateAllArtifacts() {
        return authenticateAllArtifacts;
    }

    public String getArtifactParameter() {
        return artifactParameter;
    }

    public String getServiceParameter() {
        return serviceParameter;
    }

    public String getCasServerLogoutUrl() {
        return casServerLogoutUrl;
    }

    public String getLogoutCallbackPath() {
        return logoutCallbackPath;
    }

    /**
     * 创建一个新的 {@link Builder} 实例并设置服务 URL。
     *
     * @param service 服务的 URL
     * @return {@link Builder} 实例
     * @throws IllegalArgumentException 如果传入的 service 为空
     */
    public static CasClientRegistration.Builder withService(String service) {
        Assert.hasText(service, "service cannot be empty");
        return new CasClientRegistration.Builder(service);
    }

    /**
     * 构建 CAS 客户端注册的配置对象。
     */
    public static final class Builder implements Serializable {
        private static final long serialVersionUID = 1L;

        private String casServerUrl;
        private String casServerLoginUrl;
        private String casServerLogoutUrl;
        private String logoutCallbackPath;
        private String service;
        private boolean sendRenew;
        private boolean authenticateAllArtifacts;
        private String artifactParameter;
        private String serviceParameter;

        private Builder(String service) {
            this.service = service;
        }

        /**
         * 设置 CAS 服务器的 URL。
         *
         * @param casServerUrl CAS 服务器的 URL
         * @return 当前 Builder 实例
         */
        public CasClientRegistration.Builder casServerUrl(String casServerUrl) {
            this.casServerUrl = casServerUrl;
            return this;
        }

        /**
         * 设置 CAS 服务器的登录 URL。
         *
         * @param casServerLoginUrl CAS 服务器登录 URL
         * @return 当前 Builder 实例
         */
        public CasClientRegistration.Builder casServerLoginUrl(String casServerLoginUrl) {
            this.casServerLoginUrl = casServerLoginUrl;
            return this;
        }

        /**
         * 设置 CAS 服务器的登出 URL。
         *
         * @param casServerLogoutUrl CAS 服务器登出 URL
         * @return 当前 Builder 实例
         */
        public CasClientRegistration.Builder casServerLogoutUrl(String casServerLogoutUrl) {
            this.casServerLogoutUrl = casServerLogoutUrl;
            return this;
        }

        /**
         * 设置注销回掉路径（如：/logoutCallback）
         *
         * @param logoutCallbackPath 注销回掉路径
         * @return 当前 Builder 实例
         */
        public CasClientRegistration.Builder logoutCallbackPath(String logoutCallbackPath) {
            this.logoutCallbackPath = logoutCallbackPath;
            return this;
        }

        /**
         * 设置服务 URL。
         *
         * @param service 服务 URL
         * @return 当前 Builder 实例
         */
        public CasClientRegistration.Builder service(String service) {
            this.service = service;
            return this;
        }

        /**
         * 设置是否发送 renew 参数。
         *
         * @param sendRenew 是否发送 renew 参数
         * @return 当前 Builder 实例
         */
        public CasClientRegistration.Builder sendRenew(boolean sendRenew) {
            this.sendRenew = sendRenew;
            return this;
        }

        /**
         * 设置是否认证所有 artifacts。
         *
         * @param authenticateAllArtifacts 是否认证所有 artifacts
         * @return 当前 Builder 实例
         */
        public CasClientRegistration.Builder authenticateAllArtifacts(boolean authenticateAllArtifacts) {
            this.authenticateAllArtifacts = authenticateAllArtifacts;
            return this;
        }

        /**
         * 设置 artifact 参数的名称。
         *
         * @param artifactParameter artifact 参数的名称
         * @return 当前 Builder 实例
         */
        public CasClientRegistration.Builder artifactParameter(String artifactParameter) {
            this.artifactParameter = artifactParameter;
            return this;
        }

        /**
         * 设置服务参数的名称。
         *
         * @param serviceParameter 服务参数的名称
         * @return 当前 Builder 实例
         */
        public CasClientRegistration.Builder serviceParameter(String serviceParameter) {
            this.serviceParameter = serviceParameter;
            return this;
        }

        /**
         * 构建 {@link CasClientRegistration} 实例。
         *
         * @return {@link CasClientRegistration} 实例
         * @throws IllegalArgumentException 如果必需的参数为空
         */
        public CasClientRegistration build() {
            Assert.hasText(casServerUrl, "casServerUrl cannot be empty");
            Assert.hasText(casServerLoginUrl, "casServerLoginUrl cannot be empty");
            Assert.hasText(service, "service cannot be empty");

            CasClientRegistration registration = new CasClientRegistration();
            registration.casServerUrl = this.casServerUrl;
            registration.casServerLoginUrl = this.casServerLoginUrl;
            registration.casServerLogoutUrl = this.casServerLogoutUrl;
            registration.logoutCallbackPath = this.logoutCallbackPath;
            registration.service = this.service;
            registration.sendRenew = this.sendRenew;
            registration.authenticateAllArtifacts = this.authenticateAllArtifacts;
            registration.artifactParameter = this.artifactParameter == null ? "ticket" : this.artifactParameter;
            registration.serviceParameter = this.serviceParameter == null ? "service" : this.serviceParameter;

            return registration;
        }
    }
}
