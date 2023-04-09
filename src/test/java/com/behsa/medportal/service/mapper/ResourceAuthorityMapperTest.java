package com.behsa.medportal.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResourceAuthorityMapperTest {

    private ResourceAuthorityMapper resourceAuthorityMapper;

    @BeforeEach
    public void setUp() {
        resourceAuthorityMapper = new ResourceAuthorityMapperImpl();
    }
}
