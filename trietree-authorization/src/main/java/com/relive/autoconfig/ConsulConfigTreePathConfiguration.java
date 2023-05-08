package com.relive.autoconfig;

import com.ecwid.consul.v1.ConsulClient;
import com.relive.authorization.ConsulConfigTreePathProvider;
import com.relive.authorization.TreePathProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.consul.ConditionalOnConsulEnabled;
import org.springframework.cloud.consul.config.ConsulConfigIndexes;
import org.springframework.cloud.consul.config.ConsulConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: ReLive
 * @date: 2023/5/6 20:08
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnConsulEnabled
@ConditionalOnProperty(name = "spring.cloud.consul.config.enabled", matchIfMissing = true)
public class ConsulConfigTreePathConfiguration {

    @Bean
    @ConditionalOnMissingBean(TreePathProvider.class)
    @ConditionalOnBean(ConsulConfigIndexes.class)
    public ConsulConfigTreePathProvider consulConfigTreePathProvider(ConsulConfigProperties properties, ConsulConfigIndexes indexes,
                                                                     ConsulClient consul) {
        return new ConsulConfigTreePathProvider(properties, consul, indexes.getIndexes());
    }
}
