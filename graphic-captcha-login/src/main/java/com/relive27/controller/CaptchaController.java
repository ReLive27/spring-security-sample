package com.relive27.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import com.relive27.captcha.CaptchaAuthorizationRequest;
import com.relive27.captcha.CaptchaAuthorizationRequestRepository;
import com.relive27.captcha.HttpSessionCaptchaAuthorizationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.util.Properties;

import static com.relive27.captcha.DefaultCaptchaAuthorizationRequestResolver.getIpAddr;

/**
 * @author: ReLive27
 * @date: 2023/9/9 11:23
 */
@Controller
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaAuthorizationRequestRepository<CaptchaAuthorizationRequest> authorizationRequestRepository = new HttpSessionCaptchaAuthorizationRequestRepository();

    private static final DefaultKaptcha captchaProducer;

    static {
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Properties properties = new Properties();

        properties.setProperty("kaptcha.image.width", "150");
        properties.setProperty("kaptcha.image.height", "50");

        kaptcha.setConfig(new Config(properties));
        captchaProducer = kaptcha;
    }

    @GetMapping("/captcha.jpg")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpeg");

        String captchaText = captchaProducer.createText();
        this.authorizationRequestRepository.saveAuthorizationRequest(CaptchaAuthorizationRequest.captcha(captchaText)
                .host(getIpAddr(request))
                .build(), request, response);

        BufferedImage image = captchaProducer.createImage(captchaText);
        ImageIO.write(image, "jpg", response.getOutputStream());
    }
}
