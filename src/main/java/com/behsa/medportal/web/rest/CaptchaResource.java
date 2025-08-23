package com.behsa.medportal.web.rest;

import com.behsa.medportal.security.captcha.LocalCaptchaService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CaptchaResource {

    private final LocalCaptchaService local;

    public CaptchaResource(LocalCaptchaService local) {
        this.local = local;
    }

    /** Angular calls this first: we issue an id and give it a URL to load the image. */
    @PostMapping("/captcha-endpoint")
    public Map<String, String> createCaptcha(HttpSession session) {
        var issue = local.issue();
        // Keep the "current" captcha id in session so /api/authenticate can recheck server-side.
        session.setAttribute("CAPTCHA_ID", issue.id);
        return Map.of(
            "captchaId", issue.id,
            "captchaImageUrl", "/api/captcha.png?cid=" + issue.id
        );
    }

    /** Angular <img src="..."> pulls the image from here. */
    @GetMapping(value = "/captcha.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> image(@RequestParam("cid") String id) {
        byte[] png = local.renderPng(id);
        if (png == null) return ResponseEntity.status(HttpStatus.GONE).build();
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .header(HttpHeaders.PRAGMA, "no-cache")
            .header(HttpHeaders.EXPIRES, "0")
            .body(png);
    }

    /** Optional pre-check (your UI already calls this). We DO NOT consume here. */
    @PostMapping("/captcha-validate")
    public Map<String, Object> validate(@RequestBody Map<String, String> body) {
        String id = body.get("captchaId");
        String input = body.get("userInput");
        boolean ok = local.peek(id, input);
        return Map.of("valid", ok);
    }
}
