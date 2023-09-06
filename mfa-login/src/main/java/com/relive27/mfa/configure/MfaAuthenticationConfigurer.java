package com.relive27.mfa.configure;

import com.relive27.mfa.MfaAuthenticationFilter;
import com.relive27.mfa.authentication.MfaAuthenticationProvider;
import com.relive27.mfa.totp.DefaultTotpManager;
import com.relive27.mfa.totp.MfaAuthenticationManager;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;

/**
 * @author: ReLive27
 * @date: 2023/1/9 21:32
 */
public class MfaAuthenticationConfigurer extends AbstractHttpConfigurer<MfaAuthenticationConfigurer, HttpSecurity> {

    private MfaAuthenticationManager mfaAuthenticationManager;


    public MfaAuthenticationConfigurer mfaAuthenticationManager(MfaAuthenticationManager mfaAuthenticationManager) {
        Assert.notNull(mfaAuthenticationManager, "mfaAuthenticationManager can not be null");
        this.mfaAuthenticationManager = mfaAuthenticationManager;
        return this;
    }

    @Override
    public void init(HttpSecurity http) throws Exception {
        if (this.mfaAuthenticationManager == null) {
            this.mfaAuthenticationManager = new DefaultTotpManager();
        }
        ApplicationContext applicationContext = http.getSharedObject(ApplicationContext.class);
        UserDetailsService userDetailsService = applicationContext.getBean(UserDetailsService.class);
        http.authenticationProvider(this.postProcess(new MfaAuthenticationProvider(userDetailsService, mfaAuthenticationManager)));
        super.init(http);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
        MfaAuthenticationFilter mfaAuthenticationFilter = new MfaAuthenticationFilter(authenticationManager, new AntPathRequestMatcher("/login", "POST"));
        http.addFilterBefore(this.postProcess(mfaAuthenticationFilter), UsernamePasswordAuthenticationFilter.class);
        super.configure(http);
    }
}
