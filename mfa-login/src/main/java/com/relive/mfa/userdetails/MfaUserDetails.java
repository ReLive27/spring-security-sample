package com.relive.mfa.userdetails;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author: ReLive
 * @date: 2023/1/8 22:40
 */
public class MfaUserDetails implements UserDetails, CredentialsContainer {
    private final String username;
    private String password;
    private final Set<GrantedAuthority> authorities;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;
    private final boolean enabled;
    private boolean enableMfa;
    private String secret;

    public MfaUserDetails(String username, String password, boolean enableMfa, String secret, Collection<? extends GrantedAuthority> authorities) {
        this(username, password, enableMfa, secret, true, true, true, true, authorities);
    }

    public MfaUserDetails(String username, String password, boolean enableMfa, String secret, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        Assert.isTrue(username != null && !"".equals(username) && password != null, "Cannot pass null or empty values to constructor");
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.credentialsNonExpired = credentialsNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.authorities = Collections.unmodifiableSet(new TreeSet(authorities));
        this.enableMfa = enableMfa;
        this.secret = secret;
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public String getSecret() {
        return this.secret;
    }

    public boolean isEnableMfa() {
        return enableMfa;
    }
}
