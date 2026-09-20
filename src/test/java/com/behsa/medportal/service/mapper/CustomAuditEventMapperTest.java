package com.behsa.medportal.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomAuditEventMapperTest {

    private CustomAuditEventMapper customAuditEventMapper;

    @BeforeEach
    public void setUp() {
        customAuditEventMapper = new CustomAuditEventMapperImpl();
    }
}
