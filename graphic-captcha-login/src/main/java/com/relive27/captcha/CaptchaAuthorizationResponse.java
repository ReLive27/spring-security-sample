package com.relive27.captcha;

import lombok.Data;
import org.springframework.util.Assert;

/**
 * @author: ReLive27
 * @date: 2023/9/13 09:51
 */
@Data
public class CaptchaAuthorizationResponse {
    private String captcha;

    private String host;

    public static CaptchaAuthorizationResponse.Builder host(String host) {
        return new CaptchaAuthorizationResponse.Builder().host(host);
    }

    public static CaptchaAuthorizationResponse.Builder captcha(String captcha) {
        return new CaptchaAuthorizationResponse.Builder().captcha(captcha);
    }

    public static final class Builder {

        private String captcha;

        private String host;

        public CaptchaAuthorizationResponse.Builder captcha(String captcha) {
            this.captcha = captcha;
            return this;
        }

        public CaptchaAuthorizationResponse.Builder host(String host) {
            this.host = host;
            return this;
        }


        public CaptchaAuthorizationResponse build() {
            Assert.hasText(this.captcha, "captcha cannot be empty");
            Assert.hasText(this.host, "host cannot be empty");
            CaptchaAuthorizationResponse authorizationResponse = new CaptchaAuthorizationResponse();
            authorizationResponse.captcha = this.captcha;
            authorizationResponse.host = this.host;
            return authorizationResponse;
        }
    }
}
