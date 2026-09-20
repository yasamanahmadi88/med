package com.behsa.medportal.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.behsa.medportal.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ResourceAuthorityEntityTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ResourceAuthorityEntity.class);
        ResourceAuthorityEntity resourceAuthorityEntity1 = new ResourceAuthorityEntity();
        resourceAuthorityEntity1.setId(1L);
        ResourceAuthorityEntity resourceAuthorityEntity2 = new ResourceAuthorityEntity();
        resourceAuthorityEntity2.setId(resourceAuthorityEntity1.getId());
        assertThat(resourceAuthorityEntity1).isEqualTo(resourceAuthorityEntity2);
        resourceAuthorityEntity2.setId(2L);
        assertThat(resourceAuthorityEntity1).isNotEqualTo(resourceAuthorityEntity2);
        resourceAuthorityEntity1.setId(null);
        assertThat(resourceAuthorityEntity1).isNotEqualTo(resourceAuthorityEntity2);
    }
}
