package com.behsa.medportal.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.behsa.medportal.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class InstanceDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(InstanceDTO.class);
        InstanceDTO instanceDTO1 = new InstanceDTO();
        instanceDTO1.setId(1L);
        InstanceDTO instanceDTO2 = new InstanceDTO();
        assertThat(instanceDTO1).isNotEqualTo(instanceDTO2);
        instanceDTO2.setId(instanceDTO1.getId());
        assertThat(instanceDTO1).isEqualTo(instanceDTO2);
        instanceDTO2.setId(2L);
        assertThat(instanceDTO1).isNotEqualTo(instanceDTO2);
        instanceDTO1.setId(null);
        assertThat(instanceDTO1).isNotEqualTo(instanceDTO2);
    }
}
