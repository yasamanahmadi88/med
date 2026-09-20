package com.behsa.medportal.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.behsa.medportal.service.BpmnElementAccessService;
import com.behsa.medportal.service.dto.BpmnElementAccessDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

class BpmnElementAccessResourceTest {

    @Mock
    private BpmnElementAccessService bpmnElementAccessService;

    private BpmnElementAccessResource resource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resource =
            new BpmnElementAccessResource(
                bpmnElementAccessService
            );
    }

    @Test
    void currentEndpointReturnsServerResolvedOwnerAccess() {
        BpmnElementAccessDTO access =
            new BpmnElementAccessDTO();

        access.setOwnerCode("MEDIATION");
        access.setOwnerDisplayName("Mediation Portal");

        when(bpmnElementAccessService.getCurrentAccess())
            .thenReturn(access);

        ResponseEntity<BpmnElementAccessDTO> response =
            resource.getCurrentAccess();

        assertThat(response.getStatusCode().is2xxSuccessful())
            .isTrue();

        assertThat(response.getBody()).isSameAs(access);

        verify(bpmnElementAccessService)
            .getCurrentAccess();
    }
}