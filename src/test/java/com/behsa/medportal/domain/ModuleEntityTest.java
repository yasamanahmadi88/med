package com.behsa.medportal.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.behsa.medportal.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ModuleEntityTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ModuleEntity.class);
        ModuleEntity moduleEntity1 = new ModuleEntity();
        moduleEntity1.setId(1L);
        ModuleEntity moduleEntity2 = new ModuleEntity();
        moduleEntity2.setId(moduleEntity1.getId());
        assertThat(moduleEntity1).isEqualTo(moduleEntity2);
        moduleEntity2.setId(2L);
        assertThat(moduleEntity1).isNotEqualTo(moduleEntity2);
        moduleEntity1.setId(null);
        assertThat(moduleEntity1).isNotEqualTo(moduleEntity2);
    }
}
