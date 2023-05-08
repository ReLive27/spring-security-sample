package com.relive.autoconfig;

import com.relive.authorization.AuthorizationWatch;
import com.relive.authorization.TireTreePathMatcher;
import com.relive.authorization.TreePathMatcher;
import com.relive.authorization.TreePathProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * @author: ReLive
 * @date: 2023/5/6 20:19
 */
@Configuration(proxyBeanMethods = false)
public class TreePathAuthorizationConfiguration {

    public static final String AUTHORIZATION_WATCH_TASK_SCHEDULER_NAME = "authorizationWatchTaskScheduler";

    @Bean
    public AuthorizationWatch authorizationWatch(TreePathMatcher matcher,
                                                 @Qualifier(AUTHORIZATION_WATCH_TASK_SCHEDULER_NAME) TaskScheduler taskScheduler) {
        return new AuthorizationWatch(matcher, taskScheduler);
    }

    @Bean(name = AUTHORIZATION_WATCH_TASK_SCHEDULER_NAME)
    public TaskScheduler authorizationWatchTaskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    @Bean
    @ConditionalOnMissingBean(TreePathMatcher.class)
    public TreePathMatcher treePathMatcher(TreePathProvider provider) {
        return new TireTreePathMatcher(provider);
    }
}
