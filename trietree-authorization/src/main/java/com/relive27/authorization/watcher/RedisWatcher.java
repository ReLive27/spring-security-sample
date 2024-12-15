package com.relive27.authorization.watcher;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 基于Redis缓存的监听者
 *
 * @author: ReLive27
 * @date: 2024/11/17 20:49
 */
@Slf4j
public class RedisWatcher implements Watcher {
    private Runnable updateCallback;
    private final JedisPool jedisPool;
    private final String redisChannelName;
    private SubscriberThread subThread;

    /**
     * 构造函数：使用默认的 JedisPoolConfig 初始化连接池
     *
     * @param redisIp          Redis 服务的 IP 地址
     * @param redisPort        Redis 服务的端口
     * @param redisChannelName Redis 订阅的频道名称
     * @param timeout          超时时间（毫秒）
     * @param password         Redis 密码
     */
    public RedisWatcher(String redisIp, int redisPort, String redisChannelName, int timeout, String password) {
        this.jedisPool = new JedisPool(new JedisPoolConfig(), redisIp, redisPort, timeout, password);
        this.redisChannelName = redisChannelName;
        startSub();
    }

    /**
     * 构造函数：使用指定的 JedisPoolConfig 初始化连接池
     *
     * @param config           Jedis 连接池配置
     * @param redisIp          Redis 服务的 IP 地址
     * @param redisPort        Redis 服务的端口
     * @param redisChannelName Redis 订阅的频道名称
     * @param timeout          超时时间（毫秒）
     * @param password         Redis 密码
     */
    public RedisWatcher(JedisPoolConfig config, String redisIp, int redisPort, String redisChannelName, int timeout, String password) {
        this.jedisPool = new JedisPool(config, redisIp, redisPort, timeout, password);
        this.redisChannelName = redisChannelName;
        startSub();
    }

    /**
     * 设置回调函数：Runnable 类型
     *
     * @param runnable 回调函数
     */
    @Override
    public void setUpdateCallback(Runnable runnable) {
        this.updateCallback = runnable;
        if (subThread != null) {
            subThread.setUpdateCallback(runnable);
        }
    }

    /**
     * 更新通知：向 Redis 频道发送更新消息
     */
    @Override
    public void update() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(redisChannelName, "Policy update new version.");
        } catch (Exception e) {
            log.error("Failed to publish update to Redis channel", e);
        }
    }

    /**
     * 关闭订阅线程和连接池
     */
    @Override
    public void shutdown() {
        if (subThread != null && subThread.isAlive()) {
            subThread.interrupt();
        }
        jedisPool.close();
    }


    /**
     * 启动订阅线程
     */
    private void startSub() {
        subThread = new SubscriberThread(jedisPool, redisChannelName, updateCallback);
        subThread.start();
    }
}
