package com.relive27.authorization.adapter;

import com.relive27.authorization.Model;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * RedisAdapter 是用于与 Redis 数据库交互的适配器类，提供加载、添加、移除策略规则等功能。
 * 本类使用 Jedis 客户端与 Redis 进行通信，支持不同的认证方式。
 *
 * @author: ReLive27
 * @date: 2024/11/17 20:49
 */
@Slf4j
public class RedisAdapter implements Adapter {
    private String key; // Redis 键名，用于存储策略规则
    private Jedis jedis; // Jedis 客户端实例

    /**
     * 使用默认键名初始化 RedisAdapter。
     *
     * @param host Redis 服务器的主机名或 IP 地址
     * @param port Redis 服务器的端口号
     */
    public RedisAdapter(String host, int port) {
        this(host, port, "spring:security:rules", null, null);
    }

    /**
     * 使用密码初始化 RedisAdapter。
     *
     * @param host     Redis 服务器的主机名或 IP 地址
     * @param port     Redis 服务器的端口号
     * @param password Redis 服务器的密码
     */
    public RedisAdapter(String host, int port, String password) {
        this(host, port, "spring:security:rules", null, password);
    }

    /**
     * 使用自定义键名和密码初始化 RedisAdapter。
     *
     * @param host     Redis 服务器的主机名或 IP 地址
     * @param port     Redis 服务器的端口号
     * @param key      用于存储规则的 Redis 键名
     * @param password Redis 服务器的密码
     */
    public RedisAdapter(String host, int port, String key, String password) {
        this(host, port, key, null, password);
    }

    /**
     * 使用自定义键名、用户名和密码初始化 RedisAdapter。
     *
     * @param host     Redis 服务器的主机名或 IP 地址
     * @param port     Redis 服务器的端口号
     * @param key      用于存储规则的 Redis 键名
     * @param username Redis 服务器的用户名
     * @param password Redis 服务器的密码
     */
    public RedisAdapter(String host, int port, String key, String username, String password) {
        this.key = key; // 初始化 Redis 键名
        jedis = new Jedis(host, port); // 创建 Jedis 客户端实例

        // 处理认证信息
        if (password != null) {
            if (username != null) {
                jedis.auth(username, password); // 用户名和密码认证
            } else {
                jedis.auth(password); // 仅密码认证
            }
        }

        // 测试 Redis 服务是否正常运行
        log.info("Redis service is running {}", jedis.ping());
    }

    /**
     * 从 Redis 加载策略规则并将其加载到模型中。
     *
     * @param model 需要加载规则的模型对象
     */
    @Override
    public void loadPolicy(Model model) {
        Long length = jedis.llen(this.key); // 获取列表长度
        List<String> rules = jedis.lrange(this.key, 0, length); // 从 Redis 获取所有规则
        model.loadModel(rules); // 将规则加载到模型中
    }

    /**
     * 将策略规则添加到 Redis 中。
     *
     * @param rule 需要添加的策略规则列表
     */
    @Override
    public void addPolicy(List<String> rule) {
        if (CollectionUtils.isEmpty(rule)) { // 检查规则列表是否为空
            return;
        }
        String[] rules = rule.toArray(new String[0]); // 转换为数组
        jedis.rpush(this.key, rules); // 将规则追加到 Redis 列表末尾
    }

    /**
     * 从 Redis 中移除指定的策略规则。
     *
     * @param rules 需要移除的策略规则列表
     */
    @Override
    public void removePolicy(List<String> rules) {
        // 校验规则列表是否为空
        if (CollectionUtils.isEmpty(rules)) {
            log.warn("规则列表为空，跳过移除操作");
            return;
        }

        // 遍历规则列表，逐一移除规则
        for (String rule : rules) {
            if (StringUtils.hasText(rule)) { // 确保规则非空
                jedis.lrem(this.key, 1, rule); // 从 Redis 列表中移除第一个匹配的规则
                log.info("已从键 [{}] 中移除规则 [{}]", this.key, rule);
            } else {
                log.warn("忽略空规则：{}", rule); // 记录警告日志
            }
        }
    }
}
