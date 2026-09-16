package com.behsa.medportal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.behsa.medportal.domain.BpmnElementEntity;
import com.behsa.medportal.repository.BpmnElementGroupRepository;
import com.behsa.medportal.repository.BpmnElementRepository;
import com.behsa.medportal.repository.ProductRepository;
import com.behsa.medportal.service.BpmnXmlElementScanner.XmlElementKey;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BpmnElementAccessServiceTest {

    private static final Long MCI_PRODUCT_ID = 100L;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BpmnElementRepository elementRepository;

    @Mock
    private BpmnElementGroupRepository groupRepository;

    private BpmnElementAccessService service;

    @BeforeEach
    void setUp() {
        service =
            new BpmnElementAccessService(
                productRepository,
                elementRepository,
                groupRepository,
                new BpmnXmlElementScanner()
            );
    }

    @Test
    void shouldAllowAllConfiguredMciElements() {
        List<BpmnElementEntity> allowedElements = mciAllowedElements();

        when(elementRepository.findEnabledByProductId(MCI_PRODUCT_ID))
            .thenReturn(allowedElements);

        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <bpmn:definitions
                xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                xmlns:FileReceiver="FileReceiver"
                xmlns:FileTransmitter="FileTransmitter"
                xmlns:Merger="Merger"
                xmlns:Fragmenter="Fragmenter"
                xmlns:CdrParser="CdrParser"
                xmlns:CsvTransformer="CsvTransformer">

              <bpmn:process id="Process_1">
                <FileReceiver:fileReceiver id="FR_1"/>
                <FileTransmitter:fileTransmitter id="FT_1"/>
                <Merger:merger id="M_1"/>
                <Fragmenter:fragmenter id="FG_1"/>
                <CdrParser:cdrParser id="CDR_1"/>
                <CsvTransformer:csvTransformer id="CSV_1"/>
              </bpmn:process>

            </bpmn:definitions>
            """;

        assertThat(
            service.findDisallowedElements(MCI_PRODUCT_ID, xml)
        ).isEmpty();
    }

    @Test
    void shouldRejectKafkaReceiverForMci() {
        List<BpmnElementEntity> allowedElements = mciAllowedElements();

        when(elementRepository.findEnabledByProductId(MCI_PRODUCT_ID))
            .thenReturn(allowedElements);

        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <bpmn:definitions
                xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                xmlns:KafkaReceiver="KafkaReceiver">

              <bpmn:process id="Process_1">
                <KafkaReceiver:kafkaReceiver id="KR_1"/>
              </bpmn:process>

            </bpmn:definitions>
            """;

        assertThat(
            service.findDisallowedElements(MCI_PRODUCT_ID, xml)
        ).containsExactly(
            new XmlElementKey(
                "KafkaReceiver",
                "kafkaReceiver"
            )
        );
    }

    @Test
    void shouldFailClosedWhenProductHasNoConfiguredElements() {
        when(elementRepository.findEnabledByProductId(MCI_PRODUCT_ID))
            .thenReturn(List.of());

        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <bpmn:definitions
                xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                xmlns:FileReceiver="FileReceiver">

              <bpmn:process id="Process_1">
                <FileReceiver:fileReceiver id="FR_1"/>
              </bpmn:process>

            </bpmn:definitions>
            """;

        assertThat(
            service.findDisallowedElements(MCI_PRODUCT_ID, xml)
        ).containsExactly(
            new XmlElementKey(
                "FileReceiver",
                "fileReceiver"
            )
        );
    }

    private List<BpmnElementEntity> mciAllowedElements() {
        return List.of(
            element("FileReceiver", "fileReceiver"),
            element("FileTransmitter", "fileTransmitter"),
            element("Merger", "merger"),
            element("Fragmenter", "fragmenter"),
            element("CdrParser", "cdrParser"),
            element("CsvTransformer", "csvTransformer")
        );
    }

    private BpmnElementEntity element(
        String namespaceUri,
        String localName
    ) {
        BpmnElementEntity entity =
            mock(BpmnElementEntity.class);

        when(entity.getNamespaceUri())
            .thenReturn(namespaceUri);

        when(entity.getLocalName())
            .thenReturn(localName);

        return entity;
    }
}