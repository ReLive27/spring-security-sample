package com.relive27.captcha;

import lombok.Data;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: ReLive27
 * @date: 2023/8/29 14:38
 */
@Data
public class CaptchaAuthorizationRequest {

    private String captcha;

    private String host;

    private String username;

    public static Builder host(String host) {
        return new Builder().host(host);
    }

    public static Builder captcha(String captcha) {
        return new Builder().captcha(captcha);
    }


    public static final class Builder {

        private String captcha;

        private String host;

        private String username;

        public Builder captcha(String captcha) {
            this.captcha = captcha;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public CaptchaAuthorizationRequest build() {
            if (!StringUtils.hasText(this.captcha)) {
                throw new BadCaptchaException("验证码不能为空");
            }
            Assert.hasText(this.host, "host cannot be empty");
            Assert.isTrue(this.checkHost(), "host is invalid");
            CaptchaAuthorizationRequest authorizationRequest = new CaptchaAuthorizationRequest();
            authorizationRequest.captcha = this.captcha;
            authorizationRequest.host = this.host;
            authorizationRequest.username = this.username;
            return authorizationRequest;
        }

        private boolean checkHost() {
            Pattern pattern = Pattern.compile("^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
            Matcher matcher = pattern.matcher(this.host);
            return matcher.matches();
        }
    }
}
