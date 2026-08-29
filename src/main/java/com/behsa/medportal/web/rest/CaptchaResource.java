package com.behsa.medportal.web.rest;

import com.behsa.medportal.security.captcha.LocalCaptchaService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class CaptchaResource {

    private final LocalCaptchaService localCaptchaService;

    public CaptchaResource(LocalCaptchaService localCaptchaService) {
        this.localCaptchaService = localCaptchaService;
    }

    @PostMapping("/captcha-endpoint")
    public Map<String, String> createCaptcha() {
        LocalCaptchaService.Issue issue = localCaptchaService.issue();

        return Map.of(
            "captchaId", issue.id,
            "captchaImageUrl", "/api/captcha.png?cid=" + issue.id
        );
    }

    @GetMapping(value = "/captcha.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> image(@RequestParam("cid") String captchaId) {
        byte[] png = localCaptchaService.renderPng(captchaId);

        if (png == null) {
            return ResponseEntity.status(HttpStatus.GONE).build();
        }

        return ResponseEntity
            .ok()
            .cacheControl(CacheControl.noStore())
            .header(HttpHeaders.PRAGMA, "no-cache")
            .header(HttpHeaders.EXPIRES, "0")
            .body(png);
    }
}
