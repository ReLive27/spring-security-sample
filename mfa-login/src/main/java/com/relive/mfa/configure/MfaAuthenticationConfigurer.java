package com.relive.mfa.configure;

import com.relive.mfa.TotpAuthenticationFilter;
import com.relive.mfa.authentication.TotpAuthenticationProvider;
import com.relive.mfa.totp.DefaultTotpManager;
import com.relive.mfa.totp.TotpManager;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;

/**
 * @author: ReLive
 * @date: 2023/1/9 21:32
 */
public class MfaAuthenticationConfigurer extends AbstractHttpConfigurer<MfaAuthenticationConfigurer, HttpSecurity> {

    private TotpManager totpManager;


    public MfaAuthenticationConfigurer totpManager(TotpManager totpManager) {
        Assert.notNull(totpManager, "totpManager can not be null");
        this.totpManager = totpManager;
        return this;
    }

    @Override
    public void init(HttpSecurity http) throws Exception {
        if (this.totpManager == null) {
            this.totpManager = new DefaultTotpManager();
        }
        ApplicationContext applicationContext = http.getSharedObject(ApplicationContext.class);
        UserDetailsService userDetailsService = applicationContext.getBean(UserDetailsService.class);
        http.authenticationProvider(this.postProcess(new TotpAuthenticationProvider(userDetailsService, totpManager)));
        super.init(http);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
        TotpAuthenticationFilter totpAuthenticationFilter = new TotpAuthenticationFilter(authenticationManager, new AntPathRequestMatcher("/login", "POST"));
        http.addFilterBefore(this.postProcess(totpAuthenticationFilter), UsernamePasswordAuthenticationFilter.class);
        super.configure(http);
    }
}
