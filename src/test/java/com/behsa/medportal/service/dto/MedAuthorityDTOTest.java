package com.behsa.medportal.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.behsa.medportal.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MedAuthorityDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(MedAuthorityDTO.class);
        MedAuthorityDTO medAuthorityDTO1 = new MedAuthorityDTO();
        medAuthorityDTO1.setId(1L);
        MedAuthorityDTO medAuthorityDTO2 = new MedAuthorityDTO();
        assertThat(medAuthorityDTO1).isNotEqualTo(medAuthorityDTO2);
        medAuthorityDTO2.setId(medAuthorityDTO1.getId());
        assertThat(medAuthorityDTO1).isEqualTo(medAuthorityDTO2);
        medAuthorityDTO2.setId(2L);
        assertThat(medAuthorityDTO1).isNotEqualTo(medAuthorityDTO2);
        medAuthorityDTO1.setId(null);
        assertThat(medAuthorityDTO1).isNotEqualTo(medAuthorityDTO2);
    }
}
