package com.relive27.captcha;

import lombok.Data;
import org.springframework.util.Assert;

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
            Assert.hasText(this.captcha, "captcha cannot be empty");
            Assert.hasText(this.host, "host cannot be empty");
            CaptchaAuthorizationRequest authorizationRequest = new CaptchaAuthorizationRequest();
            authorizationRequest.captcha = this.captcha;
            authorizationRequest.host = this.host;
            authorizationRequest.username = this.username;
            return authorizationRequest;
        }
    }
}
