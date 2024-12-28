package com.relive27.cas.client;

import com.relive27.cas.client.registration.CasClientRegistration;
import com.relive27.cas.client.registration.CasClientRegistrationRepository;
import com.relive27.cas.client.registration.InMemoryCasClientRegistrationRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 配置类，负责配置 CAS 客户端注册信息的存储库。
 * 使用 Spring 的 @Configuration 注解表明这是一个配置类，并通过 @EnableConfigurationProperties 启用 CasClientProperties 配置的注入。
 * 该类用于创建一个 CasClientRegistrationRepository 实例，如果容器中尚未定义该 Bean。
 *
 * @author: ReLive27
 * @date: 2024/5/6 21:00
 */
@Configuration(
        proxyBeanMethods = false
)
@Order(Ordered.HIGHEST_PRECEDENCE)
@EnableConfigurationProperties({CasClientProperties.class})
public class CasClientRegistrationRepositoryConfiguration {

    /**
     * 创建一个 InMemoryCasClientRegistrationRepository Bean，作为 CasClientRegistrationRepository 的实现。
     * <p>
     * 如果容器中没有定义 CasClientRegistrationRepository，则会调用此方法创建一个内存存储的实现。
     *
     * @param properties 从配置中加载的 CAS 客户端属性
     * @return InMemoryCasClientRegistrationRepository 实例，存储 CAS 客户端注册信息
     */
    @Bean
    @ConditionalOnMissingBean({CasClientRegistrationRepository.class})
    public InMemoryCasClientRegistrationRepository clientRegistrationRepository(CasClientProperties properties) {
        // 使用 CasClientPropertiesMapper 将配置转化为 CasClientRegistration
        CasClientRegistration registration = (new CasClientPropertiesMapper(properties)).asClientRegistrations();
        // 创建并返回 InMemoryCasClientRegistrationRepository 实例
        return new InMemoryCasClientRegistrationRepository(registration);
    }
}


