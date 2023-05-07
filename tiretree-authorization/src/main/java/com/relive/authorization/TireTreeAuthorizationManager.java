package com.relive.authorization;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author: ReLive
 * @date: 2023/5/5 20:21
 */
public class TireTreeAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    public static final String AUTHORIZE_KEY = "authorize";

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext matchResult) {
        boolean granted = this.isGranted(authentication.get(), matchResult);
        return new AuthorizationDecision(granted);
    }

    private boolean isGranted(Authentication authentication, RequestAuthorizationContext matchResult) {
        return authentication != null && authentication.isAuthenticated() && this.isAuthorized(authentication, matchResult);
    }

    private boolean isAuthorized(Authentication authentication, RequestAuthorizationContext matchResult) {
        String authorize = matchResult.getVariables().get(AUTHORIZE_KEY);
        Set<String> authorities = StringUtils.commaDelimitedListToSet(authorize);
        Iterator<? extends GrantedAuthority> iterator = authentication.getAuthorities().iterator();

        GrantedAuthority grantedAuthority;
        do {
            if (!iterator.hasNext()) {
                return false;
            }

            grantedAuthority = iterator.next();
        } while (!authorities.contains(grantedAuthority.getAuthority()));

        return true;
    }
}
