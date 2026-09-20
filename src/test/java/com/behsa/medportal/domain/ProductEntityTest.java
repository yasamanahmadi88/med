package com.behsa.medportal.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.behsa.medportal.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ProductEntityTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ProductEntity.class);
        ProductEntity productEntity1 = new ProductEntity();
        productEntity1.setId(1L);
        ProductEntity productEntity2 = new ProductEntity();
        productEntity2.setId(productEntity1.getId());
        assertThat(productEntity1).isEqualTo(productEntity2);
        productEntity2.setId(2L);
        assertThat(productEntity1).isNotEqualTo(productEntity2);
        productEntity1.setId(null);
        assertThat(productEntity1).isNotEqualTo(productEntity2);
    }
}
