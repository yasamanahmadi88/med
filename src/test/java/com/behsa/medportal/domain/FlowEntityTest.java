package com.behsa.medportal.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.behsa.medportal.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class FlowEntityTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(FlowEntity.class);
        FlowEntity flowEntity1 = new FlowEntity();
        flowEntity1.setId(1L);
        FlowEntity flowEntity2 = new FlowEntity();
        flowEntity2.setId(flowEntity1.getId());
        assertThat(flowEntity1).isEqualTo(flowEntity2);
        flowEntity2.setId(2L);
        assertThat(flowEntity1).isNotEqualTo(flowEntity2);
        flowEntity1.setId(null);
        assertThat(flowEntity1).isNotEqualTo(flowEntity2);
    }
}
