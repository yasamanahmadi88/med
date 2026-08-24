package com.behsa.medportal.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.behsa.medportal.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MedAuthorityEntityTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(MedAuthorityEntity.class);
        MedAuthorityEntity medAuthorityEntity1 = new MedAuthorityEntity();
        medAuthorityEntity1.setId(1L);
        MedAuthorityEntity medAuthorityEntity2 = new MedAuthorityEntity();
        medAuthorityEntity2.setId(medAuthorityEntity1.getId());
        assertThat(medAuthorityEntity1).isEqualTo(medAuthorityEntity2);
        medAuthorityEntity2.setId(2L);
        assertThat(medAuthorityEntity1).isNotEqualTo(medAuthorityEntity2);
        medAuthorityEntity1.setId(null);
        assertThat(medAuthorityEntity1).isNotEqualTo(medAuthorityEntity2);
    }
}
