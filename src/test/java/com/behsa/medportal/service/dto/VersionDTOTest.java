package com.behsa.medportal.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.behsa.medportal.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class VersionDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(VersionDTO.class);
        VersionDTO versionDTO1 = new VersionDTO();
        versionDTO1.setId(1L);
        VersionDTO versionDTO2 = new VersionDTO();
        assertThat(versionDTO1).isNotEqualTo(versionDTO2);
        versionDTO2.setId(versionDTO1.getId());
        assertThat(versionDTO1).isEqualTo(versionDTO2);
        versionDTO2.setId(2L);
        assertThat(versionDTO1).isNotEqualTo(versionDTO2);
        versionDTO1.setId(null);
        assertThat(versionDTO1).isNotEqualTo(versionDTO2);
    }
}
