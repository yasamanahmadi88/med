package com.behsa.medportal.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.behsa.medportal.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class VersionEntityTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(VersionEntity.class);
        VersionEntity versionEntity1 = new VersionEntity();
        versionEntity1.setId(1L);
        VersionEntity versionEntity2 = new VersionEntity();
        versionEntity2.setId(versionEntity1.getId());
        assertThat(versionEntity1).isEqualTo(versionEntity2);
        versionEntity2.setId(2L);
        assertThat(versionEntity1).isNotEqualTo(versionEntity2);
        versionEntity1.setId(null);
        assertThat(versionEntity1).isNotEqualTo(versionEntity2);
    }
}
