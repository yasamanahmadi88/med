package com.behsa.medportal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.behsa.medportal.domain.BpmnElementEntity;
import com.behsa.medportal.domain.BpmnElementGroupEntity;
import com.behsa.medportal.domain.PortalOwnerEntity;
import com.behsa.medportal.repository.BpmnElementGroupRepository;
import com.behsa.medportal.repository.BpmnElementRepository;
import com.behsa.medportal.service.BpmnXmlElementScanner.XmlElementKey;
import com.behsa.medportal.service.dto.BpmnElementAccessDTO;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class BpmnElementAccessServiceTest {

    @Mock
    private PortalOwnerService portalOwnerService;

    @Mock
    private BpmnElementRepository elementRepository;

    @Mock
    private BpmnElementGroupRepository groupRepository;

    @Mock
    private BpmnXmlElementScanner xmlElementScanner;

    private BpmnElementAccessService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        service = new BpmnElementAccessService(
            portalOwnerService,
            elementRepository,
            groupRepository,
            xmlElementScanner
        );
    }

    @Test
    void getCurrentAccessUsesActiveOwnerOnly() {
        PortalOwnerEntity owner = owner();

        BpmnElementGroupEntity group = new BpmnElementGroupEntity();
        group.setId(2000L);
        group.setGroupCode("FILE_PROCESSING");
        group.setGroupName("File Processing");
        group.setEnabled(1);

        BpmnElementEntity element = merger();

        when(portalOwnerService.getCurrentOwner()).thenReturn(owner);
        when(groupRepository.findEnabledByOwnerId(1000L))
            .thenReturn(List.of(group));
        when(elementRepository.findEnabledByOwnerId(1000L))
            .thenReturn(List.of(element));

        BpmnElementAccessDTO result = service.getCurrentAccess();

        assertThat(result.getOwnerCode()).isEqualTo("MEDIATION");
        assertThat(result.getOwnerDisplayName()).isEqualTo("Mediation Portal");

        assertThat(result.getGroups())
            .extracting(BpmnElementAccessDTO.GroupDTO::code)
            .containsExactly("FILE_PROCESSING");

        assertThat(result.getElements())
            .extracting(BpmnElementAccessDTO.ElementDTO::code)
            .containsExactly("MERGER");

        verify(portalOwnerService).getCurrentOwner();
        verify(groupRepository).findEnabledByOwnerId(1000L);
        verify(elementRepository).findEnabledByOwnerId(1000L);
    }

    @Test
    void findDisallowedElementsAllowsElementsMappedToCurrentOwner() {
        PortalOwnerEntity owner = owner();
        XmlElementKey mergerKey = new XmlElementKey("Merger", "merger");

        when(portalOwnerService.getCurrentOwner()).thenReturn(owner);
        when(xmlElementScanner.scan("<definitions />"))
            .thenReturn(Set.of(mergerKey));
        when(elementRepository.findEnabledByOwnerId(1000L))
            .thenReturn(List.of(merger()));

        assertThat(
            service.findDisallowedElements("<definitions />")
        ).isEmpty();
    }

    @Test
    void findDisallowedElementsRejectsElementNotMappedToCurrentOwner() {
        PortalOwnerEntity owner = owner();

        XmlElementKey kafka =
            new XmlElementKey("KafkaReceiver", "kafkaReceiver");

        when(portalOwnerService.getCurrentOwner()).thenReturn(owner);
        when(xmlElementScanner.scan("<definitions />"))
            .thenReturn(Set.of(kafka));
        when(elementRepository.findEnabledByOwnerId(1000L))
            .thenReturn(List.of(merger()));

        assertThat(
            service.findDisallowedElements("<definitions />")
        ).containsExactly(kafka);
    }

    @Test
    void missingOwnerFailsClosedBeforeXmlIsEvaluated() {
        when(portalOwnerService.getCurrentOwner())
            .thenThrow(
                new IllegalStateException(
                    "No enabled active portal owner is configured"
                )
            );

        assertThatThrownBy(
            () -> service.findDisallowedElements("<definitions />")
        )
            .isInstanceOf(IllegalStateException.class)
            .hasMessage(
                "No enabled active portal owner is configured"
            );

        verifyNoInteractions(
            elementRepository,
            groupRepository,
            xmlElementScanner
        );
    }

    private PortalOwnerEntity owner() {
        PortalOwnerEntity owner = new PortalOwnerEntity();
        owner.setId(1000L);
        owner.setOwnerCode("MEDIATION");
        owner.setOwnerName("Mediation");
        owner.setDisplayName("Mediation Portal");
        owner.setEnabled(1);
        return owner;
    }

    private BpmnElementEntity merger() {
        BpmnElementEntity element = new BpmnElementEntity();
        element.setId(3000L);
        element.setElementCode("MERGER");
        element.setBpmnType("Merger:Merger");
        element.setNamespaceUri("Merger");
        element.setLocalName("merger");
        element.setPaletteAction("create.merger");
        element.setDisplayName("Merger");
        element.setEnabled(1);
        element.setSortOrder(10);
        return element;
    }
}