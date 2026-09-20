package com.behsa.medportal.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.behsa.medportal.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ResourceAuthorityDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ResourceAuthorityDTO.class);
        ResourceAuthorityDTO resourceAuthorityDTO1 = new ResourceAuthorityDTO();
        resourceAuthorityDTO1.setId(1L);
        ResourceAuthorityDTO resourceAuthorityDTO2 = new ResourceAuthorityDTO();
        assertThat(resourceAuthorityDTO1).isNotEqualTo(resourceAuthorityDTO2);
        resourceAuthorityDTO2.setId(resourceAuthorityDTO1.getId());
        assertThat(resourceAuthorityDTO1).isEqualTo(resourceAuthorityDTO2);
        resourceAuthorityDTO2.setId(2L);
        assertThat(resourceAuthorityDTO1).isNotEqualTo(resourceAuthorityDTO2);
        resourceAuthorityDTO1.setId(null);
        assertThat(resourceAuthorityDTO1).isNotEqualTo(resourceAuthorityDTO2);
    }
}
