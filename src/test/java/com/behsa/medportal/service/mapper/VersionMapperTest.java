package com.behsa.medportal.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VersionMapperTest {

    private VersionMapper versionMapper;

    @BeforeEach
    public void setUp() {
        versionMapper = new VersionMapperImpl();
    }
}
