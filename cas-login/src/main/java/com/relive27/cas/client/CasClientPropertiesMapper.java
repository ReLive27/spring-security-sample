package com.relive27.cas.client;

import com.relive27.cas.client.registration.CasClientRegistration;
import org.springframework.boot.context.properties.PropertyMapper;

/**
 * CasClientPropertiesMapper 用于将 CasClientProperties 中的配置信息映射为 CasClientRegistration 实例。
 * 它主要的作用是根据配置属性生成一个用于 CAS 客户端注册的对象。
 *
 * @author: ReLive27
 * @date: 2024/5/6 21:10
 */
public class CasClientPropertiesMapper {
    private final CasClientProperties properties;

    /**
     * 构造函数，接受 CasClientProperties 实例。
     *
     * @param properties CAS 客户端配置属性
     */
    public CasClientPropertiesMapper(CasClientProperties properties) {
        this.properties = properties;
    }

    /**
     * 将 CAS 客户端属性映射为 CasClientRegistration 实例。
     *
     * @return 构建好的 CasClientRegistration 实例
     */
    public CasClientRegistration asClientRegistrations() {
        return getClientRegistration(properties);
    }

    /**
     * 根据给定的 CasClientProperties 配置构建 CasClientRegistration 实例。
     * 该方法会将配置中的各个属性（如 casServerUrl、casServerLoginUrl 等）映射到 CasClientRegistration 的相应字段。
     *
     * @param properties CasClientProperties 配置实例
     * @return 构建好的 CasClientRegistration 实例
     */
    private static CasClientRegistration getClientRegistration(CasClientProperties properties) {
        // 使用 CasClientRegistration.Builder 创建一个新的注册对象
        CasClientRegistration.Builder builder = CasClientRegistration.withService(properties.getService());
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();

        // 映射 casServerUrl 属性
        map.from(properties::getCasServerUrl).to(builder::casServerUrl);

        // 映射 casServerLoginUrl 属性
        map.from(properties::getCasServerLoginUrl).to(builder::casServerLoginUrl);

        // 映射 casServerLogoutUrl 属性
        map.from(properties::getCasServerLogoutUrl).to(builder::casServerLogoutUrl);

        // 映射 artifactParameter 属性
        map.from(properties::getArtifactParameter).to(builder::artifactParameter);

        // 映射 serviceParameter 属性
        map.from(properties::getServiceParameter).to(builder::serviceParameter);

        // 映射 authenticateAllArtifacts 属性
        map.from(properties::isAuthenticateAllArtifacts).to(builder::authenticateAllArtifacts);

        // 映射 sendRenew 属性
        map.from(properties::isSendRenew).to(builder::sendRenew);

        // 返回构建好的 CasClientRegistration 实例
        return builder.build();
    }
}
