package com.behsa.medportal.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.behsa.medportal.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ConfigDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ConfigDTO.class);
        ConfigDTO configDTO1 = new ConfigDTO();
        configDTO1.setId(1L);
        ConfigDTO configDTO2 = new ConfigDTO();
        assertThat(configDTO1).isNotEqualTo(configDTO2);
        configDTO2.setId(configDTO1.getId());
        assertThat(configDTO1).isEqualTo(configDTO2);
        configDTO2.setId(2L);
        assertThat(configDTO1).isNotEqualTo(configDTO2);
        configDTO1.setId(null);
        assertThat(configDTO1).isNotEqualTo(configDTO2);
    }
}
