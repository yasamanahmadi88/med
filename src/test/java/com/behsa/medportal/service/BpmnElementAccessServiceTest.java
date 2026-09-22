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
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class BpmnElementAccessServiceTest {

    /** The scanner is mocked, so these only have to be distinguishable from one another. */
    private static final String SUBMITTED = "<definitions submitted=\"true\" />";
    private static final String PERSISTED = "<definitions persisted=\"true\" />";

    private static final XmlElementKey CDR_PARSER = new XmlElementKey("CdrParser", "cdrParser");

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

    @Test
    void restrictedElementIsRefusedWhenThereIsNoStoredDiagram() {
        catalogHoldingMergerAndCdrParser();
        when(xmlElementScanner.scan(SUBMITTED)).thenReturn(Set.of(CDR_PARSER));

        // A create carries nothing over, so the catalog entry alone never admits it.
        assertThat(service.findDisallowedElements(SUBMITTED)).containsExactly(CDR_PARSER);
    }

    @Test
    void restrictedElementSurvivesAnUpdateThatKeepsItWhereItWas() {
        catalogHoldingMergerAndCdrParser();
        when(xmlElementScanner.scan(SUBMITTED)).thenReturn(Set.of(CDR_PARSER));
        when(xmlElementScanner.scanInstances(SUBMITTED)).thenReturn(Map.of("Cdr_1", CDR_PARSER));
        when(xmlElementScanner.scanInstances(PERSISTED)).thenReturn(Map.of("Cdr_1", CDR_PARSER));
        when(xmlElementScanner.countInstances(SUBMITTED)).thenReturn(Map.of(CDR_PARSER, 1L));

        assertThat(service.findDisallowedElements(SUBMITTED, PERSISTED)).isEmpty();
    }

    @Test
    void keepingOneRestrictedElementDoesNotLicenceASecond() {
        catalogHoldingMergerAndCdrParser();
        when(xmlElementScanner.scan(SUBMITTED)).thenReturn(Set.of(CDR_PARSER));
        when(xmlElementScanner.scanInstances(SUBMITTED)).thenReturn(Map.of("Cdr_1", CDR_PARSER, "Cdr_2", CDR_PARSER));
        when(xmlElementScanner.scanInstances(PERSISTED)).thenReturn(Map.of("Cdr_1", CDR_PARSER));
        when(xmlElementScanner.countInstances(SUBMITTED)).thenReturn(Map.of(CDR_PARSER, 2L));

        assertThat(service.findDisallowedElements(SUBMITTED, PERSISTED)).containsExactly(CDR_PARSER);
    }

    @Test
    void restrictedElementAtAnIdTheStoredDiagramNeverHeldIsRefused() {
        catalogHoldingMergerAndCdrParser();
        when(xmlElementScanner.scan(SUBMITTED)).thenReturn(Set.of(CDR_PARSER));
        when(xmlElementScanner.scanInstances(SUBMITTED)).thenReturn(Map.of("Cdr_9", CDR_PARSER));
        when(xmlElementScanner.scanInstances(PERSISTED)).thenReturn(Map.of("Cdr_1", CDR_PARSER));
        when(xmlElementScanner.countInstances(SUBMITTED)).thenReturn(Map.of(CDR_PARSER, 1L));

        assertThat(service.findDisallowedElements(SUBMITTED, PERSISTED)).containsExactly(CDR_PARSER);
    }

    @Test
    void restrictedElementCarryingNoIdIsRefusedEvenBesideOneThatWasCarriedOver() {
        catalogHoldingMergerAndCdrParser();
        when(xmlElementScanner.scan(SUBMITTED)).thenReturn(Set.of(CDR_PARSER));
        // An id-keyed scan cannot see the second instance; the count is what gives it away.
        when(xmlElementScanner.scanInstances(SUBMITTED)).thenReturn(Map.of("Cdr_1", CDR_PARSER));
        when(xmlElementScanner.scanInstances(PERSISTED)).thenReturn(Map.of("Cdr_1", CDR_PARSER));
        when(xmlElementScanner.countInstances(SUBMITTED)).thenReturn(Map.of(CDR_PARSER, 2L));

        assertThat(service.findDisallowedElements(SUBMITTED, PERSISTED)).containsExactly(CDR_PARSER);
    }

    @Test
    void anElementMissingFromTheCatalogIsStillRefusedOnAnUpdate() {
        XmlElementKey kafka = new XmlElementKey("KafkaReceiver", "kafkaReceiver");

        catalogHoldingMergerAndCdrParser();
        when(xmlElementScanner.scan(SUBMITTED)).thenReturn(Set.of(kafka));
        when(xmlElementScanner.scanInstances(SUBMITTED)).thenReturn(Map.of("Kafka_1", kafka));
        when(xmlElementScanner.scanInstances(PERSISTED)).thenReturn(Map.of("Kafka_1", kafka));
        when(xmlElementScanner.countInstances(SUBMITTED)).thenReturn(Map.of(kafka, 1L));

        // Carry-over is a concession to the restricted codes only, not to anything unmapped.
        assertThat(service.findDisallowedElements(SUBMITTED, PERSISTED)).containsExactly(kafka);
    }

    private void catalogHoldingMergerAndCdrParser() {
        when(portalOwnerService.getCurrentOwner()).thenReturn(owner());
        when(elementRepository.findEnabledByOwnerId(1000L)).thenReturn(List.of(merger(), cdrParser()));
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

    /** Mapped to the Owner like any other element, and withheld anyway by its element code. */
    private BpmnElementEntity cdrParser() {
        BpmnElementEntity element = new BpmnElementEntity();
        element.setId(3001L);
        element.setElementCode("CDR_PARSER");
        element.setBpmnType("CdrParser:CdrParser");
        element.setNamespaceUri(CDR_PARSER.namespaceUri());
        element.setLocalName(CDR_PARSER.localName());
        element.setPaletteAction("create.cdrParser-module");
        element.setDisplayName("CDR Parser Module");
        element.setEnabled(1);
        element.setSortOrder(200);
        return element;
    }
}