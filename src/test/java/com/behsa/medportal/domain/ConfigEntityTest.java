package com.behsa.medportal.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.behsa.medportal.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ConfigEntityTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ConfigEntity.class);
        ConfigEntity configEntity1 = new ConfigEntity();
        configEntity1.setId(1L);
        ConfigEntity configEntity2 = new ConfigEntity();
        configEntity2.setId(configEntity1.getId());
        assertThat(configEntity1).isEqualTo(configEntity2);
        configEntity2.setId(2L);
        assertThat(configEntity1).isNotEqualTo(configEntity2);
        configEntity1.setId(null);
        assertThat(configEntity1).isNotEqualTo(configEntity2);
    }
}
