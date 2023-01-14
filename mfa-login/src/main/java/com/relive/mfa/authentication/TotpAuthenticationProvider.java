package com.relive.mfa.authentication;

import com.relive.mfa.exception.TotpAuthenticationException;
import com.relive.mfa.totp.TotpManager;
import com.relive.mfa.userdetails.MfaUserDetails;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.Assert;

/**
 * @author: ReLive
 * @date: 2023/1/7 22:52
 */
public class TotpAuthenticationProvider implements AuthenticationProvider {
    private final TotpManager totpManager;
    private final UserDetailsService userDetailsService;


    public TotpAuthenticationProvider(UserDetailsService userDetailsService,
                                      TotpManager totpManager) {
        Assert.notNull(userDetailsService, "userDetailsService cannot be null");
        Assert.notNull(totpManager, "totpManager cannot be null");
        this.userDetailsService = userDetailsService;
        this.totpManager = totpManager;
    }


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        TotpAuthenticationToken totpAuthenticationToken = (TotpAuthenticationToken) authentication;

        String username = totpAuthenticationToken.getPrincipal().toString();
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
        if (userDetails == null) {
            throw new InternalAuthenticationServiceException("UserDetailsService returned null, which is an interface contract violation");
        }

        if (userDetails instanceof MfaUserDetails) {
            MfaUserDetails mfaUserDetails = (MfaUserDetails) userDetails;
            if (mfaUserDetails.isEnableMfa()) {
                if (!this.totpManager.validCode(mfaUserDetails.getSecret(),
                        totpAuthenticationToken.getCredentials())) {

                    throw new TotpAuthenticationException("Code verification failed", null);
                }
            }

            return new TotpAuthenticationToken(username, totpAuthenticationToken.getCredentials(), true);

        }
        throw new TotpAuthenticationException("MfaUserDetails must be an instance of UserDetails", null);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return TotpAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
