package com.behsa.medportal.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.behsa.medportal.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CustomAuditEventEntityTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CustomAuditEventEntity.class);
        CustomAuditEventEntity customAuditEventEntity1 = new CustomAuditEventEntity();
        customAuditEventEntity1.setId(1L);
        CustomAuditEventEntity customAuditEventEntity2 = new CustomAuditEventEntity();
        customAuditEventEntity2.setId(customAuditEventEntity1.getId());
        assertThat(customAuditEventEntity1).isEqualTo(customAuditEventEntity2);
        customAuditEventEntity2.setId(2L);
        assertThat(customAuditEventEntity1).isNotEqualTo(customAuditEventEntity2);
        customAuditEventEntity1.setId(null);
        assertThat(customAuditEventEntity1).isNotEqualTo(customAuditEventEntity2);
    }
}
