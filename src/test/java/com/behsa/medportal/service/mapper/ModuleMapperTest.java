package com.behsa.medportal.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ModuleMapperTest {

    private ModuleMapper moduleMapper;

    @BeforeEach
    public void setUp() {
        moduleMapper = new ModuleMapperImpl();
    }
}
