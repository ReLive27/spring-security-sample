package com.relive27.authorization;

import java.util.Map;

/**
 * 策略执行器
 *
 * @author: ReLive27
 * @date: 2023/5/5 20:35
 */
public interface PolicyEnforcer {

    /**
     * 判断是否有访问权限
     *
     * @param value
     * @return
     */
    boolean hasAccess(String... value);

    /**
     * 获取权限信息
     *
     * @param value
     * @return
     */
    Map<String, String> fetchAuthorize(String... value);
}
