package com.behsa.medportal.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FlowMapperTest {

    private FlowMapper flowMapper;

    @BeforeEach
    public void setUp() {
        flowMapper = new FlowMapperImpl();
    }
}
