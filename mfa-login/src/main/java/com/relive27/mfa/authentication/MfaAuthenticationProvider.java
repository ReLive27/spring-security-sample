package com.relive27.mfa.authentication;

import com.relive27.mfa.exception.MfaAuthenticationException;
import com.relive27.mfa.totp.MfaAuthenticationManager;
import com.relive27.mfa.userdetails.MfaUserDetails;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.Assert;

/**
 * @author: ReLive27
 * @date: 2023/1/7 22:52
 */
public class MfaAuthenticationProvider implements AuthenticationProvider {
    private final MfaAuthenticationManager mfaAuthenticationManager;
    private final UserDetailsService userDetailsService;


    public MfaAuthenticationProvider(UserDetailsService userDetailsService,
                                     MfaAuthenticationManager mfaAuthenticationManager) {
        Assert.notNull(userDetailsService, "userDetailsService cannot be null");
        Assert.notNull(mfaAuthenticationManager, "mfaAuthenticationManager cannot be null");
        this.userDetailsService = userDetailsService;
        this.mfaAuthenticationManager = mfaAuthenticationManager;
    }


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        MfaAuthenticationToken mfaAuthenticationToken = (MfaAuthenticationToken) authentication;

        String username = mfaAuthenticationToken.getPrincipal().toString();
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
        if (userDetails == null) {
            throw new InternalAuthenticationServiceException("UserDetailsService returned null, which is an interface contract violation");
        }

        if (userDetails instanceof MfaUserDetails) {
            MfaUserDetails mfaUserDetails = (MfaUserDetails) userDetails;
            if (mfaUserDetails.isEnableMfa()) {
                if (!this.mfaAuthenticationManager.validCode(mfaUserDetails.getSecret(),
                        mfaAuthenticationToken.getCredentials())) {

                    throw new MfaAuthenticationException("Code verification failed", null);
                }
            }

            return new MfaAuthenticationToken(username, mfaAuthenticationToken.getCredentials(), true);

        }
        throw new MfaAuthenticationException("MfaUserDetails must be an instance of UserDetails", null);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return MfaAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
