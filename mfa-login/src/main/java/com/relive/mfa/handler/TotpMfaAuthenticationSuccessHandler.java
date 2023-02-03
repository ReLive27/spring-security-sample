package com.relive.mfa.handler;

import com.relive.mfa.context.TotpTokenContext;
import com.relive.mfa.context.TotpTokenContextHolder;
import com.relive.mfa.convert.TotpHttpMessageConverter;
import com.relive.mfa.jwt.TokenGenerator;
import com.relive.mfa.totp.DefaultTotpManager;
import com.relive.mfa.totp.TotpManager;
import com.relive.mfa.userdetails.MfaUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author: ReLive
 * @date: 2023/1/9 21:41
 */
@Slf4j
public class TotpMfaAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private TotpManager toTpManager = new DefaultTotpManager();
    private final HttpMessageConverter<TotpMfaResponse> totpHttpMessageConverter =
            new TotpHttpMessageConverter();
    private final TokenGenerator<Jwt> tokenGenerator;
    private final UserDetailsManager userDetailsManager;

    public TotpMfaAuthenticationSuccessHandler(TokenGenerator<Jwt> tokenGenerator,
                                               UserDetailsManager userDetailsManager) {
        Assert.notNull(tokenGenerator, "tokenGenerator can not be null");
        Assert.notNull(userDetailsManager, "userDetailsManager can not be null");
        this.tokenGenerator = tokenGenerator;
        this.userDetailsManager = userDetailsManager;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) authentication;
        MfaUserDetails userDetails = (MfaUserDetails) authenticationToken.getPrincipal();
        if (userDetails.isEnableMfa()) {

            if (!StringUtils.hasText(userDetails.getSecret())) {
                String secret = toTpManager.generateSecret();
                userDetails.setSecret(secret);
                this.userDetailsManager.updateUser(userDetails);
                String uriForImage;
                try {
                    uriForImage = toTpManager.getUriForImage(userDetails.getUsername(), secret, "http://127.0.0.1:8080");
                } catch (Exception e) {
                    log.error("Error getting QR code image", e);
                    TotpMfaResponse totpMfaResponse = TotpMfaResponse.unauthenticated("Error getting QR code image", "bind", null);
                    response.setStatus(HttpStatus.BAD_REQUEST.value());
                    this.sendTotpMfaResponse(request, response, totpMfaResponse);
                    return;
                }
                TotpMfaResponse totpMfaResponse = TotpMfaResponse.unauthenticated("The current account is not bound to the token app", "bind", uriForImage);
                this.sendTotpMfaResponse(request, response, totpMfaResponse);
                return;
            }
            TotpTokenContext totpTokenContext = TotpTokenContextHolder.getTotpTokenContext();
            if (totpTokenContext == null || !totpTokenContext.isMfa()) {
                TotpMfaResponse totpMfaResponse = TotpMfaResponse.unauthenticated("dynamic password error", "enable", null);
                this.sendTotpMfaResponse(request, response, totpMfaResponse);
                return;
            }
        }

        Jwt jwt = this.tokenGenerator.generate(authentication);
        TotpMfaResponse totpMfaResponse = TotpMfaResponse.authenticated(userDetails.isEnableMfa() ? "enable" : "disabled", jwt.getTokenValue());
        this.sendTotpMfaResponse(request, response, totpMfaResponse);
    }

    public void setToTpManager(TotpManager toTpManager) {
        this.toTpManager = toTpManager;
    }


    private void sendTotpMfaResponse(HttpServletRequest request, HttpServletResponse response,
                                     TotpMfaResponse totpMfaResponse) throws IOException {
        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        this.totpHttpMessageConverter.write(totpMfaResponse, null, httpResponse);
    }


}
