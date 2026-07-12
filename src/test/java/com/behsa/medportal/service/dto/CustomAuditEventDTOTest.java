package com.behsa.medportal.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.behsa.medportal.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CustomAuditEventDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CustomAuditEventDTO.class);
        CustomAuditEventDTO customAuditEventDTO1 = new CustomAuditEventDTO();
        customAuditEventDTO1.setId(1L);
        CustomAuditEventDTO customAuditEventDTO2 = new CustomAuditEventDTO();
        assertThat(customAuditEventDTO1).isNotEqualTo(customAuditEventDTO2);
        customAuditEventDTO2.setId(customAuditEventDTO1.getId());
        assertThat(customAuditEventDTO1).isEqualTo(customAuditEventDTO2);
        customAuditEventDTO2.setId(2L);
        assertThat(customAuditEventDTO1).isNotEqualTo(customAuditEventDTO2);
        customAuditEventDTO1.setId(null);
        assertThat(customAuditEventDTO1).isNotEqualTo(customAuditEventDTO2);
    }
}
