package com.behsa.medportal.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.behsa.medportal.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class InstanceEntityTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(InstanceEntity.class);
        InstanceEntity instanceEntity1 = new InstanceEntity();
        instanceEntity1.setId(1L);
        InstanceEntity instanceEntity2 = new InstanceEntity();
        instanceEntity2.setId(instanceEntity1.getId());
        assertThat(instanceEntity1).isEqualTo(instanceEntity2);
        instanceEntity2.setId(2L);
        assertThat(instanceEntity1).isNotEqualTo(instanceEntity2);
        instanceEntity1.setId(null);
        assertThat(instanceEntity1).isNotEqualTo(instanceEntity2);
    }
}
