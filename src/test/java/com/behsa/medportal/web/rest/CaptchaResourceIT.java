package com.behsa.medportal.web.rest;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.security.captcha.LocalCaptchaService;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
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
    void verifyAndConsumeShouldOnlyWorkOnce() throws Exception {
        LocalCaptchaService service = new LocalCaptchaService();

        LocalCaptchaService.Issue issue = service.issue();
        String captchaText = captchaText(service, issue.id);

        assertThat(service.verifyAndConsume(issue.id, captchaText)).isTrue();
        assertThat(service.verifyAndConsume(issue.id, captchaText)).isFalse();
    }

    @SuppressWarnings("unchecked")
    private String captchaText(LocalCaptchaService service, String captchaId) throws Exception {
        Field storeField = LocalCaptchaService.class.getDeclaredField("store");
        storeField.setAccessible(true);
        Map<String, ?> store = (Map<String, ?>) storeField.get(service);

        Object entry = store.get(captchaId);
        assertThat(entry).isNotNull();

        Field textField = entry.getClass().getDeclaredField("text");
        textField.setAccessible(true);
        return (String) textField.get(entry);
    }
}
