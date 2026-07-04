package com.behsa.medportal.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfigMapperTest {

    private ConfigMapper configMapper;

    @BeforeEach
    public void setUp() {
        configMapper = new ConfigMapperImpl();
    }
}
