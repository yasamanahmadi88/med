package com.behsa.medportal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.behsa.medportal.domain.PortalOwnerEntity;
import com.behsa.medportal.repository.PortalOwnerRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PortalOwnerServiceTest {

    @Mock
    private PortalOwnerRepository portalOwnerRepository;

    private PortalOwnerService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new PortalOwnerService(portalOwnerRepository);
    }

    @Test
    void getCurrentOwnerReturnsConfiguredEnabledOwner() {
        PortalOwnerEntity owner = new PortalOwnerEntity();
        owner.setId(1000L);
        owner.setOwnerCode("MEDIATION");
        owner.setOwnerName("Mediation");
        owner.setDisplayName("Mediation Portal");
        owner.setEnabled(1);

        when(portalOwnerRepository.findActiveOwner())
            .thenReturn(Optional.of(owner));

        PortalOwnerEntity result = service.getCurrentOwner();

        assertThat(result).isSameAs(owner);
        assertThat(result.getId()).isEqualTo(1000L);
        assertThat(result.getOwnerCode()).isEqualTo("MEDIATION");
        assertThat(result.getDisplayName()).isEqualTo("Mediation Portal");
        assertThat(result.getEnabled()).isEqualTo(1);

        verify(portalOwnerRepository).findActiveOwner();
    }

    @Test
    void getCurrentOwnerFailsClosedWhenConfigurationIsMissing() {
        when(portalOwnerRepository.findActiveOwner())
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCurrentOwner())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("No enabled active portal owner is configured");

        verify(portalOwnerRepository).findActiveOwner();
    }
}