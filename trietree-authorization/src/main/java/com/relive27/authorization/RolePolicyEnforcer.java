package com.relive27.authorization;

import com.relive27.authorization.adapter.Adapter;
import com.relive27.authorization.watcher.Watcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.relive27.authorization.RoleAuthorizationManager.AUTHORIZE_KEY;

/**
 * 基于角色的策略执行器
 *
 * @author: ReLive27
 * @date: 2023/5/5 20:44
 */
@Slf4j
public class RolePolicyEnforcer implements PolicyEnforcer, ManagementEnforcer {
    // 策略模型，用于存储和匹配访问策略
    private Model model = new Model();

    // 持久化适配器，用于保存和加载策略
    private Adapter adapter;

    // 观察者，用于在策略变更时触发通知
    private Watcher watcher;

    // 是否自动通知观察者
    private boolean autoNotifyWatcher = true;

    // 是否自动保存到适配器
    private boolean autoSave = true;

    /**
     * 检查路径是否有访问权限
     *
     * @param path 路径数组，通常只使用第一个元素
     * @return 如果路径有匹配的策略，则返回 true，否则返回 false
     */
    @Override
    public boolean hasAccess(String... path) {
        return StringUtils.hasText(this.model.findMatch(path[0]));
    }

    /**
     * 获取路径对应的授权信息
     *
     * @param path 路径数组，通常只使用第一个元素
     * @return 如果找到匹配的角色，则返回一个包含角色的 Map，否则返回空 Map
     */
    @Override
    public Map<String, String> fetchAuthorize(String... path) {
        String extractRole = this.model.findMatch(path[0]);
        return StringUtils.hasText(extractRole) ?
                Collections.singletonMap(AUTHORIZE_KEY, extractRole) : Collections.emptyMap();
    }

    /**
     * 添加多个策略规则
     *
     * @param rules 策略规则列表
     * @return 添加是否成功
     */
    @Override
    public boolean addPolicies(List<String> rules) {
        // 自动保存到持久化适配器
        if (adapter != null && autoSave) {
            try {
                adapter.addPolicy(rules);
            } catch (Exception e) {
                log.error("添加策略时发生异常: " + e.getMessage());
                return false;
            }
        }

        // 更新策略模型
        model.addModel(rules);

        // 通知观察者
        if (watcher != null && autoNotifyWatcher) {
            watcher.update();
        }

        return true;
    }

    /**
     * 加载所有策略规则
     */
    @Override
    public void loadPolicy() {
        if (adapter != null) {
            // 重置模型
            model.resetModel();

            // 从持久化适配器加载策略
            adapter.loadPolicy(model);

            // 打印模型，用于调试
            model.printModel();
        }
    }

    /**
     * 移除多个策略规则
     *
     * @param rules 策略规则列表
     * @return 移除是否成功
     */
    @Override
    public boolean removePolicies(List<String> rules) {
        // 从持久化适配器移除
        if (adapter != null && autoSave) {
            try {
                adapter.removePolicy(rules);
            } catch (Exception e) {
                log.error("移除策略时发生异常: " + e.getMessage());
                return false;
            }
        }

        // 从模型中移除
        model.removeModel(rules);

        // 通知观察者
        if (watcher != null && autoNotifyWatcher) {
            watcher.update();
        }

        return true;
    }

    // Getter 和 Setter 方法
    public Adapter getAdapter() {
        return adapter;
    }

    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
    }

    public Watcher getWatcher() {
        return watcher;
    }

    public void setWatcher(Watcher watcher) {
        this.watcher = watcher;
        // 设置观察者的回调为加载策略
        watcher.setUpdateCallback(this::loadPolicy);
    }
}
