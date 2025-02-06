package com.relive27.cas.client;

import com.relive27.cas.client.registration.CasClientRegistration;
import com.relive27.cas.client.registration.CasClientRegistrationRepository;
import com.relive27.handler.SimpleLogoutSuccessHandler;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.ssl.HttpURLConnectionFactory;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.*;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * {@code CASAuthorizationConfiguration} 是用于配置 Spring Security 中 CAS (Central Authentication Service)
 * 认证和授权的类。它扩展了 {@link AbstractHttpConfigurer} 并通过 {@link HttpSecurity} 配置 CAS 认证和相关过滤器。
 * <p>
 * 该配置类包括了以下内容：
 * <ul>
 *     <li>初始化 CAS 认证提供程序（{@link CasAuthenticationProvider}）和验证器（{@link Cas20ServiceTicketValidator}）</li>
 *     <li>配置 CAS 客户端注册信息（{@link CasClientRegistrationRepository}）</li>
 *     <li>设置 CAS 认证过滤器（{@link CasAuthenticationFilter}）</li>
 *     <li>配置 CAS 单点登出过滤器（{@link SingleSignOutFilter}）</li>
 *     <li>配置登出过滤器（{@link LogoutFilter}）</li>
 * </ul>
 * </p>
 * 本配置类主要用于集成 CAS 认证机制，使得应用可以支持基于 CAS 的单点登录（SSO）和相关功能。
 * </p>
 *
 * @author: ReLive27
 * @date: 2022/12/1 13:38
 */
public class CASAuthorizationConfiguration extends AbstractHttpConfigurer<CASAuthorizationConfiguration, HttpSecurity> {

    //客户端cas 登录URL，用于处理Ticket
    private String loginCasUrl;

    //客户端登出URL
    private String logoutUrl;
    //成功认证后的处理器
    private AuthenticationSuccessHandler successHandler;

    /**
     * 客户端cas 登录URL，用于处理Ticket
     *
     * @param loginCasUrl 客户端cas 登录URL
     * @return 返回当前的 CASAuthorizationConfiguration 对象，实现链式调用
     */
    public CASAuthorizationConfiguration loginCasUrl(String loginCasUrl) {
        this.loginCasUrl = loginCasUrl;
        return this;
    }

    /**
     * 客户端登出URL
     *
     * @param logoutUrl 客户端登出URL
     * @return 返回当前的 CASAuthorizationConfiguration 对象，实现链式调用
     */
    public CASAuthorizationConfiguration logoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
        return this;
    }

    /**
     * 设置认证成功后的处理器。
     * <p>
     * 此方法用于配置在用户成功认证后执行的操作。通过传入一个 {@link AuthenticationSuccessHandler}，
     * </p>
     *
     * @param successHandler 认证成功后的处理器，通常用于定义认证成功后的行为。
     * @return 返回当前的 {@link CASAuthorizationConfiguration} 对象，支持链式调用。
     */
    public CASAuthorizationConfiguration successHandler(AuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
        return this;
    }


    /**
     * 初始化方法，配置 CAS 认证提供程序和相关验证器。
     *
     * @param http {@link HttpSecurity} 对象，用于配置 Spring Security。
     * @throws Exception 如果配置过程中发生错误。
     */
    @Override
    public void init(HttpSecurity http) throws Exception {
        // 创建 CAS 认证提供程序
        CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();

        // 获取 CAS 客户端注册信息
        CasClientRegistrationRepository casClientRegistrationRepository = getCasClientRegistrationRepository(http);
        CasClientRegistration casClientRegistration = casClientRegistrationRepository.loadClientRegistration();

        // 配置 CAS 认证提供程序的服务属性
        casAuthenticationProvider.setServiceProperties(createServiceProperties(http, casClientRegistration));

        // 创建 CAS 服务票据验证器
        Cas20ServiceTicketValidator cas20ServiceTicketValidator = new Cas20ServiceTicketValidator(casClientRegistration.getCasServerUrl());

        // 配置 SSL 上下文工厂，如果使用 https
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(casClientRegistration.getCasServerUrl()).build();
        if ("https".equals(uriComponents.getScheme())) {
            cas20ServiceTicketValidator.setURLConnectionFactory(new CasHttpURLConnectionFactory());
        }
        cas20ServiceTicketValidator.setEncoding("UTF-8");

        // 配置票据验证器
        casAuthenticationProvider.setTicketValidator(cas20ServiceTicketValidator);

        // 配置用户服务
        UserDetailsByNameServiceWrapper<CasAssertionAuthenticationToken> userDetailsByNameServiceWrapper = new UserDetailsByNameServiceWrapper<>();
        userDetailsByNameServiceWrapper.setUserDetailsService(getUserDetailsService(http));
        casAuthenticationProvider.setAuthenticationUserDetailsService(userDetailsByNameServiceWrapper);

        // 设置认证提供程序的唯一密钥
        casAuthenticationProvider.setKey("casAuthenticationProviderKey");

        // 注册 CAS 认证提供程序
        http.authenticationProvider(this.postProcess(casAuthenticationProvider));

        super.init(http);
    }

    /**
     * 配置 CAS 认证过滤器、单点登出过滤器以及登出过滤器。
     *
     * @param http {@link HttpSecurity} 对象，用于配置 Spring Security。
     * @throws Exception 如果配置过程中发生错误。
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);

        // 获取 CAS 客户端注册信息
        CasClientRegistrationRepository casClientRegistrationRepository = getCasClientRegistrationRepository(http);
        CasClientRegistration casClientRegistration = casClientRegistrationRepository.loadClientRegistration();

        // 配置 CAS 认证过滤器
        CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
        casAuthenticationFilter.setServiceProperties(createServiceProperties(http, casClientRegistration));
        casAuthenticationFilter.setAuthenticationManager(authenticationManager);
        if (StringUtils.hasText(this.loginCasUrl)) {
            casAuthenticationFilter.setFilterProcessesUrl(this.loginCasUrl);
        }
        if (this.successHandler != null) {
            casAuthenticationFilter.setAuthenticationSuccessHandler(successHandler);
        }

        // 将 CAS 认证过滤器添加到 Spring Security 过滤链中
        http.addFilterBefore(this.postProcess(casAuthenticationFilter), UsernamePasswordAuthenticationFilter.class);

        // 配置单点登出过滤器
        SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
        if (StringUtils.hasText(casClientRegistration.getLogoutCallbackPath())) {
            singleSignOutFilter.setLogoutCallbackPath(casClientRegistration.getLogoutCallbackPath());
            LogoutFilter logoutCallbackFilter = new LogoutFilter(new SimpleUrlLogoutSuccessHandler(), new SecurityContextLogoutHandler());
            logoutCallbackFilter.setFilterProcessesUrl(casClientRegistration.getLogoutCallbackPath());
            http.addFilterBefore(this.postProcess(logoutCallbackFilter), LogoutFilter.class);
        }
        singleSignOutFilter.setIgnoreInitConfiguration(true);
        http.addFilterBefore(this.postProcess(singleSignOutFilter), CasAuthenticationFilter.class);

        // 配置登出过滤器
        LogoutFilter logoutFilter = new LogoutFilter(new SimpleLogoutSuccessHandler(casClientRegistration.getCasServerLogoutUrl() + "?service=" + casClientRegistration.getService()), new SecurityContextLogoutHandler());
        if (StringUtils.hasText(this.logoutUrl)) {
            logoutFilter.setFilterProcessesUrl(this.logoutUrl);
        }
        http.addFilterBefore(this.postProcess(logoutFilter), LogoutFilter.class);

        super.configure(http);
    }

    /**
     * 获取 CAS 认证入口点，配置 CAS 登录 URL 和服务属性。
     *
     * @param httpSecurity {@link HttpSecurity} 对象，用于配置 Spring Security。
     * @return {@link CasJsonAuthenticationEntryPoint} 实例。
     */
    public static CasJsonAuthenticationEntryPoint getCasAuthenticationEntryPoint(HttpSecurity httpSecurity) {
        CasClientRegistrationRepository casClientRegistrationRepository = getCasClientRegistrationRepository(httpSecurity);
        CasClientRegistration registration = casClientRegistrationRepository.loadClientRegistration();

        // 配置 CAS 认证入口点
        CasJsonAuthenticationEntryPoint casAuthenticationEntryPoint = new CasJsonAuthenticationEntryPoint();
        casAuthenticationEntryPoint.setLoginUrl(registration.getCasServerLoginUrl());
        casAuthenticationEntryPoint.setServiceProperties(createServiceProperties(httpSecurity, registration));
        casAuthenticationEntryPoint.setHttpStatus(HttpStatus.OK);
        return casAuthenticationEntryPoint;
    }

    /**
     * 创建并配置 CAS 服务属性，用于 CAS 认证。
     *
     * @param httpSecurity          {@link HttpSecurity} 对象，用于配置 Spring Security。
     * @param casClientRegistration CAS 客户端注册信息。
     * @return 配置好的 {@link ServiceProperties} 对象。
     */
    static ServiceProperties createServiceProperties(HttpSecurity httpSecurity, CasClientRegistration casClientRegistration) {
        ServiceProperties serviceProperties = httpSecurity.getSharedObject(ServiceProperties.class);
        if (serviceProperties == null) {
            serviceProperties = getOptionalBean(httpSecurity, ServiceProperties.class);
            if (serviceProperties == null) {
                serviceProperties = new ServiceProperties();
                serviceProperties.setService(casClientRegistration.getService());
                // 设置为 true时，告知 CAS 登录服务不允许单点登录，用户必须重新输入用户名和密码
                serviceProperties.setSendRenew(casClientRegistration.isSendRenew());
                serviceProperties.setAuthenticateAllArtifacts(casClientRegistration.isAuthenticateAllArtifacts());
                serviceProperties.setServiceParameter(casClientRegistration.getServiceParameter());
                serviceProperties.setArtifactParameter(casClientRegistration.getArtifactParameter());
            }
            httpSecurity.setSharedObject(ServiceProperties.class, serviceProperties);
        }
        return serviceProperties;
    }

    /**
     * 获取用户服务。如果没有找到，则使用内存中的用户管理器。
     *
     * @param httpSecurity {@link HttpSecurity} 对象，用于配置 Spring Security。
     * @return 用户服务实例。
     */
    static UserDetailsService getUserDetailsService(HttpSecurity httpSecurity) {
        UserDetailsService userDetailsService = httpSecurity.getSharedObject(UserDetailsService.class);
        if (userDetailsService == null) {
            userDetailsService = getOptionalBean(httpSecurity, UserDetailsService.class);
            if (userDetailsService == null) {
                userDetailsService = new InMemoryUserDetailsManager();
            }
            httpSecurity.setSharedObject(UserDetailsService.class, userDetailsService);
        }

        return userDetailsService;
    }

    /**
     * 获取 CAS 客户端注册信息存储库。
     *
     * @param httpSecurity {@link HttpSecurity} 对象，用于配置 Spring Security。
     * @return {@link CasClientRegistrationRepository} 实例。
     */
    static CasClientRegistrationRepository getCasClientRegistrationRepository(HttpSecurity httpSecurity) {
        CasClientRegistrationRepository casClientRegistrationRepository = httpSecurity.getSharedObject(CasClientRegistrationRepository.class);
        if (casClientRegistrationRepository == null) {
            casClientRegistrationRepository = getOptionalBean(httpSecurity, CasClientRegistrationRepository.class);
            if (casClientRegistrationRepository == null) {
                throw new IllegalStateException("CAS Client Registration Repository not found");
            }

            httpSecurity.setSharedObject(CasClientRegistrationRepository.class, casClientRegistrationRepository);
        }

        return casClientRegistrationRepository;
    }

    /**
     * 获取可选的 Spring Bean 实例。
     *
     * @param httpSecurity {@link HttpSecurity} 对象，用于配置 Spring Security。
     * @param type         要获取的 Bean 类型。
     * @param <T>          Bean 类型。
     * @return Bean 实例，如果没有找到则返回 null。
     */
    public static <T> T getOptionalBean(HttpSecurity httpSecurity, Class<T> type) {
        Map<String, T> beansMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(
                httpSecurity.getSharedObject(ApplicationContext.class), type);
        if (beansMap.size() > 1) {
            throw new NoUniqueBeanDefinitionException(type, beansMap.size(),
                    "Expected single matching bean of type '" + type.getName() + "' but found " +
                            beansMap.size() + ": " + StringUtils.collectionToCommaDelimitedString(beansMap.keySet()));
        }
        return (!beansMap.isEmpty() ? beansMap.values().iterator().next() : null);
    }


    /**
     * {@link CasHttpURLConnectionFactory} 是一个实现 {@link HttpURLConnectionFactory} 接口的工厂类，
     * 用于构建自定义的 {@link HttpURLConnection} 实例，特别是为 {@link HttpURLConnection} 配置 SSL 上下文。
     * <p>
     * 该工厂类会创建一个不进行验证的 SSL 上下文，忽略服务器和客户端的 SSL 证书验证，
     * 并设置一个默认的 {@link SSLSocketFactory} 和 {@link HostnameVerifier}，以支持与 CAS 服务器的安全连接。
     * </p>
     */
    static class CasHttpURLConnectionFactory implements HttpURLConnectionFactory {

        /**
         * 构建并返回一个自定义配置的 {@link HttpURLConnection} 对象。
         *
         * @param urlConnection 现有的 {@link URLConnection} 实例，通常是 {@link HttpsURLConnection}
         * @return 配置后的 {@link HttpURLConnection} 实例
         * @throws IllegalStateException 如果 SSL 上下文初始化失败
         */
        @Override
        public HttpURLConnection buildHttpURLConnection(URLConnection urlConnection) {
            SSLContext sslContext = null;
            try {
                // 创建 SSL 上下文并初始化
                sslContext = SSLContext.getInstance("SSL");
                sslContext.init(new KeyManager[]{}, new TrustManager[]{new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
                            throws CertificateException {
                        // 客户端信任检查不做任何操作
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
                            throws CertificateException {
                        // 服务器信任检查不做任何操作
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        // 返回一个空的受信任证书列表
                        return new X509Certificate[0];
                    }
                }}, null);

                // 获取 SSL 套接字工厂
                SSLSocketFactory socketFactory = sslContext.getSocketFactory();

                // 如果是 HttpsURLConnection，配置 SSL 套接字工厂和主机名验证器
                if (urlConnection instanceof HttpsURLConnection) {
                    HttpsURLConnection httpsURLConnection = (HttpsURLConnection) urlConnection;
                    httpsURLConnection.setSSLSocketFactory(socketFactory);
                    httpsURLConnection.setHostnameVerifier((s, l) -> true); // 始终信任主机
                }

                return (HttpURLConnection) urlConnection;

            } catch (Exception e) {
                // 如果发生异常，则抛出非法状态异常
                throw new IllegalStateException("Failed to configure SSL context", e);
            }
        }
    }


}
