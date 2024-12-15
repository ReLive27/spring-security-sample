package com.relive27.authorization.watcher;

/**
 * @author: ReLive27
 * @date: 2024/11/17 20:42
 */
public interface Watcher {

    void setUpdateCallback(Runnable runnable);

    void update();

    void shutdown();
}
