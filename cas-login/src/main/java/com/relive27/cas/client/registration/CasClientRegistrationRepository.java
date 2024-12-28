package com.relive27.cas.client.registration;


/**
 * 用于加载CAS客户端注册信息接口。
 * <p>
 * 该接口的实现类负责从不同的存储介质（如数据库、配置文件、内存等）中加载CAS客户端的注册信息。
 * 通常用于在CAS认证过程中读取客户端的配置信息，包括CAS服务器URL、服务参数、认证方式等。
 * </p>
 *
 * @author: ReLive27
 * @date: 2024/5/6 20:58
 */
public interface CasClientRegistrationRepository {

    /**
     * 加载CAS客户端注册信息。
     * <p>
     * 该方法返回一个包含CAS客户端注册配置信息的 {@link CasClientRegistration} 对象。
     * </p>
     *
     * @return {@link CasClientRegistration} CAS客户端的注册信息。
     */
    CasClientRegistration loadClientRegistration();
}

