package com.behsa.medportal.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.behsa.medportal.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ResourceEntityTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ResourceEntity.class);
        ResourceEntity resourceEntity1 = new ResourceEntity();
        resourceEntity1.setId(1L);
        ResourceEntity resourceEntity2 = new ResourceEntity();
        resourceEntity2.setId(resourceEntity1.getId());
        assertThat(resourceEntity1).isEqualTo(resourceEntity2);
        resourceEntity2.setId(2L);
        assertThat(resourceEntity1).isNotEqualTo(resourceEntity2);
        resourceEntity1.setId(null);
        assertThat(resourceEntity1).isNotEqualTo(resourceEntity2);
    }
}
