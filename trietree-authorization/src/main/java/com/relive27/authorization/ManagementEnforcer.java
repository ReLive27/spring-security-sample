package com.relive27.authorization;

import java.util.List;

/**
 * @author: ReLive27
 * @date: 2024/12/14 22:23
 */
public interface ManagementEnforcer {

    /**
     * 添加指定策略
     *
     * @param rules
     * @return
     */
    boolean addPolicies(List<String> rules);

    /**
     * 加载策略
     */
    void loadPolicy();

    /**
     * 删除指定策略
     *
     * @param rules
     * @return
     */
    boolean removePolicies(List<String> rules);

}
