package com.relive.authorization;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.Assert;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author: ReLive
 * @date: 2023/5/5 21:14
 */
@Slf4j
public class AuthorizationWatch implements ApplicationEventPublisherAware, SmartLifecycle {
    private final TaskScheduler taskScheduler;

    private final AtomicBoolean running = new AtomicBoolean(false);


    private ApplicationEventPublisher publisher;

    private boolean firstTime = true;

    private ScheduledFuture<?> watchFuture;

    private final TreePathMatcher matcher;


    public AuthorizationWatch(TreePathMatcher matcher) {
        this(matcher, getTaskScheduler());
    }

    public AuthorizationWatch(TreePathMatcher matcher, TaskScheduler taskScheduler) {
        Assert.notNull(matcher, "treePathMatcher cannot be null");
        this.matcher = matcher;
        this.taskScheduler = taskScheduler;
    }

    private static ThreadPoolTaskScheduler getTaskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.initialize();
        return taskScheduler;
    }

    @Override
    public void start() {
        if (this.running.compareAndSet(false, true)) {
            this.watchFuture = this.taskScheduler.scheduleWithFixedDelay(this::watchConfigKeyValues,
                    1000);
        }
    }

    @Override
    public void stop(Runnable callback) {
        this.stop();
        callback.run();
    }

    @Override
    public int getPhase() {
        return 0;
    }

    @Override
    public void stop() {
        if (this.running.compareAndSet(true, false) && this.watchFuture != null) {
            this.watchFuture.cancel(true);
        }
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }


    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    @Timed("task.authorize")
    public void watchConfigKeyValues() {
        if (!this.running.get()) {
            return;
        }

        if (this.firstTime) {
            if (this.matcher.buildTree()) {
                this.firstTime = false;
                this.publisher.publishEvent(new RefreshAuthorityEvent(this));
            }
        } else {
            if (this.matcher.rebuildTree()) {
                this.publisher.publishEvent(new RefreshAuthorityEvent(this));
            }
        }
    }
}
