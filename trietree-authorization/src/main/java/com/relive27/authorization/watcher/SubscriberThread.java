package com.relive27.authorization.watcher;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author: ReLive27
 * @date: 2024/12/15 19:22
 */
@Slf4j
public class SubscriberThread extends Thread {
    private final JedisPool jedisPool;
    private final PolicySubscriber subscriber;
    private final String channel;

    /**
     * 构造函数，初始化订阅线程
     *
     * @param jedisPool      Redis 连接池
     * @param channel        要订阅的频道
     * @param updateCallback 更新回调函数
     */
    public SubscriberThread(JedisPool jedisPool, String channel, Runnable updateCallback) {
        super("SubThread"); // 为线程设置名称
        this.jedisPool = jedisPool;
        this.channel = channel;
        this.subscriber = new PolicySubscriber(updateCallback); // 初始化订阅者
    }

    /**
     * 设置更新回调函数
     *
     * @param runnable 更新回调函数
     */
    public void setUpdateCallback(Runnable runnable) {
        subscriber.setUpdateCallback(runnable);
    }

    /**
     * 订阅线程的主逻辑
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) { // 检查线程中断状态
            try (Jedis jedis = jedisPool.getResource()) {
                log.info("正在订阅 Redis 频道: {}", channel);
                jedis.subscribe(subscriber, channel); // 开始订阅 Redis 频道
            } catch (Exception e) {
                log.error("Redis 订阅异常", e);
            } finally {
                try {
                    // 等待 500 毫秒后重新尝试订阅
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    log.warn("订阅线程被中断，退出中...");
                    Thread.currentThread().interrupt(); // 恢复中断状态
                    break;
                }
            }
        }
    }
}
