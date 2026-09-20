package com.behsa.medportal.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.behsa.medportal.domain.PortalOwnerEntity;
import com.behsa.medportal.service.PortalOwnerService;
import com.behsa.medportal.service.dto.PortalOwnerDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PortalOwnerResourceTest {

    @Mock
    private PortalOwnerService portalOwnerService;

    private PortalOwnerResource resource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resource = new PortalOwnerResource(portalOwnerService);
    }

    @Test
    void getCurrentOwnerReturnsOnlyPublicOwnerContext() {
        PortalOwnerEntity owner = new PortalOwnerEntity();
        owner.setId(1000L);
        owner.setOwnerCode("MEDIATION");
        owner.setOwnerName("Mediation");
        owner.setDisplayName("Mediation Portal");
        owner.setDescription("Internal description");
        owner.setEnabled(1);

        when(portalOwnerService.getCurrentOwner()).thenReturn(owner);

        PortalOwnerDTO result = resource.getCurrentOwner();

        assertThat(result.getCode()).isEqualTo("MEDIATION");
        assertThat(result.getDisplayName()).isEqualTo("Mediation Portal");

        verify(portalOwnerService).getCurrentOwner();
    }

    @Test
    void getCurrentOwnerDoesNotHideFailClosedConfigurationErrors() {
        when(portalOwnerService.getCurrentOwner())
            .thenThrow(new IllegalStateException("No enabled active portal owner is configured"));

        assertThatThrownBy(resource::getCurrentOwner)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("No enabled active portal owner is configured");

        verify(portalOwnerService).getCurrentOwner();
    }
}