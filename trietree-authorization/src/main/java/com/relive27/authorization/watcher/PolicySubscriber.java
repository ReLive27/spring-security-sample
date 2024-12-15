package com.relive27.authorization.watcher;

import redis.clients.jedis.JedisPubSub;

/**
 * 策略更新订阅者
 *
 * @author: ReLive27
 * @date: 2024/12/15 19:16
 */
public class PolicySubscriber extends JedisPubSub {
    private Runnable runnable;

    /**
     * 构造函数，初始化 Runnable 类型的回调函数
     *
     * @param updateCallback 回调函数
     */
    public PolicySubscriber(Runnable updateCallback) {
        this.runnable = updateCallback;
    }

    /**
     * 设置 Runnable 类型的回调函数
     *
     * @param runnable 回调函数
     */
    public synchronized void setUpdateCallback(Runnable runnable) {
        this.runnable = runnable;
    }

    /**
     * 当收到消息时触发
     *
     * @param channel 接收到消息的频道
     * @param message 消息内容
     */
    @Override
    public void onMessage(String channel, String message) {
        // 如果 Runnable 回调不为空，则运行它
        if (runnable != null) {
            runnable.run();
        }
    }
}
