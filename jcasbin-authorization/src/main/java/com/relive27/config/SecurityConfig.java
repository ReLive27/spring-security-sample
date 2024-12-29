package com.relive27.config;

import com.relive27.authorization.JcasbinAuthorizationManager;
import org.casbin.adapter.JDBCAdapter;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

/**
 * @author: ReLive27
 * @date: 2024/12/29 21:35
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    /**
     * 配置 Spring Security 的 SecurityFilterChain，使用 JCasbin 进行访问控制
     *
     * @param httpSecurity Spring Security 的 HttpSecurity 配置对象
     * @param enforcer     JCasbin 的 Enforcer 对象，用于权限控制
     * @return 配置好的 SecurityFilterChain
     * @throws Exception 如果配置过程中发生异常
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, Enforcer enforcer) throws Exception {
        httpSecurity
                // 配置所有请求通过 JCasbinAuthorizationManager 进行权限控制
                .authorizeHttpRequests(authorizeRequest ->
                        authorizeRequest.anyRequest().access(new JcasbinAuthorizationManager(enforcer)))

                // 禁用 CSRF 防护。如果是 API 服务，通常禁用 CSRF
                .csrf(csrf -> csrf.disable())

                // 启用表单登录，使用 Spring Security 的默认登录页面和登录流程
                .formLogin(Customizer.withDefaults());

        // 返回配置好的 SecurityFilterChain
        return httpSecurity.build();
    }

    /**
     * 配置 JCasbin 的 Enforcer 实例，并连接到数据库进行策略存储
     *
     * @param dataSource 数据源，用于连接数据库并加载策略
     * @return 配置好的 Enforcer 对象
     * @throws Exception 如果创建 Enforcer 或加载策略时发生异常
     */
    @Bean
    Enforcer enforcer(DataSource dataSource) throws Exception {
        // 创建 JDBC 适配器，连接数据库
        JDBCAdapter jdbcAdapter = new JDBCAdapter(dataSource);

        // 创建 JCasbin 的模型配置
        Model model = new Model();

        // 定义请求规则 r：sub(主体)，obj(对象)，act(操作)
        model.addDef("r", "r", "sub, obj, act");

        // 定义策略规则 p：sub(主体)，obj(对象)，act(操作)
        model.addDef("p", "p", "sub, obj, act");

        // 定义角色关系规则 g：表示用户和角色的关系
        model.addDef("g", "g", "_, _");

        // 定义效果规则 e：如果存在允许的策略，返回 true
        model.addDef("e", "e", "some(where (p.eft == allow))");

        // 定义模型匹配规则 m：用于判断是否匹配请求和策略
        model.addDef("m", "m", "g(r.sub, p.sub) && r.obj == p.obj && r.act == p.act");
        // 创建 Enforcer 实例，将模型和数据库适配器结合
        Enforcer enforcer = new Enforcer(model, jdbcAdapter);
        // 返回配置好的 Enforcer
        return enforcer;
    }
}

