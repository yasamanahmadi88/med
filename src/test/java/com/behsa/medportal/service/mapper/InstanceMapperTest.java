package com.behsa.medportal.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InstanceMapperTest {

    private InstanceMapper instanceMapper;

    @BeforeEach
    public void setUp() {
        instanceMapper = new InstanceMapperImpl();
    }
}
