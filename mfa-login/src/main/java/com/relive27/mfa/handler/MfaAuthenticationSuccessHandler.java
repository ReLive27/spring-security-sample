package com.relive27.mfa.handler;

import com.relive27.mfa.context.MfaAuthenticationTokenContextHolder;
import com.relive27.mfa.context.MfaTokenContext;
import com.relive27.mfa.convert.MfaAuthenticationHttpMessageConverter;
import com.relive27.mfa.jwt.TokenGenerator;
import com.relive27.mfa.totp.DefaultTotpManager;
import com.relive27.mfa.totp.MfaAuthenticationManager;
import com.relive27.mfa.userdetails.MfaUserDetails;
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
 * @author: ReLive27
 * @date: 2023/1/9 21:41
 */
@Slf4j
public class MfaAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private MfaAuthenticationManager mfaAuthenticationManager = new DefaultTotpManager();
    private final HttpMessageConverter<MfaAuthenticationResponse> mfaAuthenticationHttpMessageConverter =
            new MfaAuthenticationHttpMessageConverter();
    private final TokenGenerator<Jwt> tokenGenerator;
    private final UserDetailsManager userDetailsManager;

    public MfaAuthenticationSuccessHandler(TokenGenerator<Jwt> tokenGenerator,
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
                String secret = mfaAuthenticationManager.generateSecret();
                userDetails.setSecret(secret);
                this.userDetailsManager.updateUser(userDetails);
                String uriForImage;
                try {
                    uriForImage = mfaAuthenticationManager.getUriForImage(userDetails.getUsername(), secret, "http://127.0.0.1:8080");
                } catch (Exception e) {
                    log.error("Error getting QR code image", e);
                    MfaAuthenticationResponse mfaAuthenticationResponse = MfaAuthenticationResponse.unauthenticated("Error getting QR code image", "bind", HttpStatus.BAD_REQUEST, null);
                    this.sendMfaResponse(request, response, mfaAuthenticationResponse);
                    return;
                }
                MfaAuthenticationResponse mfaAuthenticationResponse = MfaAuthenticationResponse.unauthenticated("The current account is not bound to the token app", "bind", HttpStatus.OK, uriForImage);
                this.sendMfaResponse(request, response, mfaAuthenticationResponse);
                return;
            }
            MfaTokenContext mfaTokenContext = MfaAuthenticationTokenContextHolder.getMfaTokenContext();
            if (mfaTokenContext == null || !mfaTokenContext.isMfa()) {
                MfaAuthenticationResponse mfaAuthenticationResponse = MfaAuthenticationResponse.unauthenticated("dynamic password error", "enable", HttpStatus.OK, null);
                this.sendMfaResponse(request, response, mfaAuthenticationResponse);
                return;
            }
        }

        Jwt jwt = this.tokenGenerator.generate(authentication);
        MfaAuthenticationResponse mfaAuthenticationResponse = MfaAuthenticationResponse.authenticated(userDetails.isEnableMfa() ? "enable" : "disabled", jwt.getTokenValue());
        this.sendMfaResponse(request, response, mfaAuthenticationResponse);
    }

    public void setMfaAuthenticationManager(MfaAuthenticationManager mfaAuthenticationManager) {
        this.mfaAuthenticationManager = mfaAuthenticationManager;
    }


    private void sendMfaResponse(HttpServletRequest request, HttpServletResponse response,
                                 MfaAuthenticationResponse mfaAuthenticationResponse) throws IOException {
        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        this.mfaAuthenticationHttpMessageConverter.write(mfaAuthenticationResponse, null, httpResponse);
    }


}
