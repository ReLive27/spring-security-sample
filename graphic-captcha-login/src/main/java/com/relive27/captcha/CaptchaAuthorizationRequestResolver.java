package com.relive27.captcha;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author: ReLive27
 * @date: 2023/8/31 17:32
 */
public interface CaptchaAuthorizationRequestResolver {

    CaptchaAuthorizationRequest resolve(HttpServletRequest request) throws IOException;

}
