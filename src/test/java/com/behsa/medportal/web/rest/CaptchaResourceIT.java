package com.behsa.medportal.web.rest;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.security.captcha.LocalCaptchaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@IntegrationTest
class CaptchaResourceIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGenerateCaptcha() throws Exception {
        mockMvc
            .perform(post("/api/captcha-endpoint"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.captchaId").exists())
            .andExpect(jsonPath("$.captchaImageUrl").exists());
    }

    @Test
    void testValidateCaptcha() throws Exception {
        String requestBody = "{\"captchaId\":\"test-id\",\"userInput\":\"test-input\"}";

        mockMvc
            .perform(post("/api/captcha-validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valid").exists());
    }


    @Test
    void verifyAndConsumeShouldOnlyWorkOnce() {
        LocalCaptchaService service = new LocalCaptchaService();

        LocalCaptchaService.Issue issue = service.issue();

        // todo
    }
}
