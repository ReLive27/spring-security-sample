package com.relive27.authorization.adapter;

import com.relive27.authorization.Model;

import java.util.List;

/**
 * 权限存储适配器
 *
 * @author: ReLive27
 * @date: 2024/11/17 20:36
 */
public interface Adapter {
    /**
     * 从存储中加载策略
     *
     * @param model
     */
    void loadPolicy(Model model);

    /**
     * 添加策略到存储中
     *ø
     * @param rule
     */
    void addPolicy(List<String> rule);

    /**
     * 从存储中删除一条策略
     *
     * @param rule
     */
    void removePolicy(List<String> rule);
}

