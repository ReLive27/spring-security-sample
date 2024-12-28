package com.relive27.cas.client.registration;

import org.springframework.util.Assert;

/**
 * 从内存中加载CAS客户端注册信息的实现类。
 * <p>
 * 本实现将CAS客户端的注册信息硬编码在内存中。在实际应用中，可以将其扩展为从数据库或配置文件中加载注册信息。
 * </p>
 *
 * @author: ReLive27
 * @date: 2024/5/6 20:59
 */
public class InMemoryCasClientRegistrationRepository implements CasClientRegistrationRepository {

    // CAS客户端的注册信息
    private final CasClientRegistration clientRegistration;

    /**
     * 构造方法，初始化一个示例CAS客户端注册信息。
     */
    public InMemoryCasClientRegistrationRepository(CasClientRegistration registration) {
        Assert.notNull(registration, "registration cannot be null");
        this.clientRegistration = registration;
    }

    /**
     * 加载CAS客户端注册信息。
     * <p>
     * 返回在构造方法中初始化的CAS客户端注册信息。
     * </p>
     *
     * @return {@link CasClientRegistration} CAS客户端的注册信息。
     */
    @Override
    public CasClientRegistration loadClientRegistration() {
        return this.clientRegistration;
    }
}

