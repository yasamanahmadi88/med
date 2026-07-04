package com.behsa.medportal.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MedAuthorityMapperTest {

    private MedAuthorityMapper medAuthorityMapper;

    @BeforeEach
    public void setUp() {
        medAuthorityMapper = new MedAuthorityMapperImpl();
    }
}
