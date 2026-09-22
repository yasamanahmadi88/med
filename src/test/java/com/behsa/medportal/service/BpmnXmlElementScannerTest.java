package com.behsa.medportal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.behsa.medportal.service.BpmnXmlElementScanner.XmlElementKey;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BpmnXmlElementScannerTest {

    private static final String BPMN_NS =
        "http://www.omg.org/spec/BPMN/20100524/MODEL";

    private final BpmnXmlElementScanner scanner =
        new BpmnXmlElementScanner();

    @Test
    void shouldDetectAllMciElementsAndIgnoreStructuralElements() {
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

                <bpmn:sequenceFlow
                    id="Flow_1"
                    sourceRef="FR_1"
                    targetRef="FT_1"/>

              </bpmn:process>
            </bpmn:definitions>
            """;

        Set<XmlElementKey> result = scanner.scan(xml);

        assertThat(result).containsExactlyInAnyOrder(
            new XmlElementKey("FileReceiver", "fileReceiver"),
            new XmlElementKey("FileTransmitter", "fileTransmitter"),
            new XmlElementKey("Merger", "merger"),
            new XmlElementKey("Fragmenter", "fragmenter"),
            new XmlElementKey("CdrParser", "cdrParser"),
            new XmlElementKey("CsvTransformer", "csvTransformer")
        );

        assertThat(result)
            .doesNotContain(new XmlElementKey(BPMN_NS, "sequenceFlow"));
    }

    @Test
    void shouldUseNamespaceUriAndNotPrefix() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <bpmn:definitions
                xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                xmlns:x="FileReceiver">

              <bpmn:process id="Process_1">
                <x:fileReceiver id="FR_1"/>
              </bpmn:process>

            </bpmn:definitions>
            """;

        assertThat(scanner.scan(xml))
            .containsExactly(
                new XmlElementKey("FileReceiver", "fileReceiver")
            );
    }

    @Test
    void shouldRejectDoctypeAndExternalEntity() {
        String xml = """
            <?xml version="1.0"?>
            <!DOCTYPE foo [
              <!ENTITY xxe SYSTEM "file:///etc/passwd">
            ]>
            <bpmn:definitions
                xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL">
              <bpmn:process id="Process_1">
                <bpmn:task id="Task_1" name="&xxe;"/>
              </bpmn:process>
            </bpmn:definitions>
            """;

        assertThatThrownBy(() -> scanner.scan(xml))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid or unsafe BPMN XML");
    }

    @Test
    void shouldReturnEmptySetForBlankXml() {
        assertThat(scanner.scan(null)).isEmpty();
        assertThat(scanner.scan("")).isEmpty();
        assertThat(scanner.scan("   ")).isEmpty();
    }

    @Test
    void shouldMapElementIdsToTheirTypesInScanInstances() {
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

                <bpmn:sequenceFlow
                    id="Flow_1"
                    sourceRef="FR_1"
                    targetRef="FT_1"/>

              </bpmn:process>
            </bpmn:definitions>
            """;

        Map<String, XmlElementKey> result = scanner.scanInstances(xml);

        assertThat(result)
            .containsEntry("FR_1", new XmlElementKey("FileReceiver", "fileReceiver"))
            .containsEntry("FT_1", new XmlElementKey("FileTransmitter", "fileTransmitter"))
            .containsEntry("M_1", new XmlElementKey("Merger", "merger"))
            .containsEntry("FG_1", new XmlElementKey("Fragmenter", "fragmenter"))
            .containsEntry("CDR_1", new XmlElementKey("CdrParser", "cdrParser"))
            .containsEntry("CSV_1", new XmlElementKey("CsvTransformer", "csvTransformer"))
            .doesNotContainKey("Flow_1");
    }

    @Test
    void shouldIgnoreElementsWithoutIdsInScanInstances() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <bpmn:definitions
                xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                xmlns:FileReceiver="FileReceiver">

              <bpmn:process id="Process_1">
                <FileReceiver:fileReceiver id="FR_1"/>
                <FileReceiver:fileReceiver/>

              </bpmn:process>

            </bpmn:definitions>
            """;

        Map<String, XmlElementKey> result = scanner.scanInstances(xml);

        assertThat(result)
            .containsExactly(Map.entry("FR_1", new XmlElementKey("FileReceiver", "fileReceiver")));
    }

    @Test
    void shouldReturnEmptyMapForBlankXmlInScanInstances() {
        assertThat(scanner.scanInstances(null)).isEmpty();
        assertThat(scanner.scanInstances("")).isEmpty();
        assertThat(scanner.scanInstances("   ")).isEmpty();
    }

    @Test
    void shouldRejectUnsafeXmlInScanInstances() {
        String xml = """
            <?xml version="1.0"?>
            <!DOCTYPE foo [
              <!ENTITY xxe SYSTEM "file:///etc/passwd">
            ]>
            <bpmn:definitions
                xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL">
              <bpmn:process id="Process_1">
                <bpmn:task id="Task_1" name="&xxe;"/>
              </bpmn:process>
            </bpmn:definitions>
            """;

        assertThatThrownBy(() -> scanner.scanInstances(xml))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid or unsafe BPMN XML");
    }

    @Test
    void shouldCountEveryInstanceIncludingOnesWithoutAnId() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <bpmn:definitions
                xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                xmlns:CdrParser="CdrParser">

              <bpmn:process id="Process_1">

                <CdrParser:cdrParser id="CDR_1"/>
                <CdrParser:cdrParser/>
                <CdrParser:cdrParser id="CDR_1"/>

                <bpmn:sequenceFlow id="Flow_1"/>

              </bpmn:process>
            </bpmn:definitions>
            """;

        // scanInstances collapses all three onto one entry: two share an id and one has none.
        assertThat(scanner.scanInstances(xml)).hasSize(1);

        assertThat(scanner.countInstances(xml))
            .containsExactly(Map.entry(new XmlElementKey("CdrParser", "cdrParser"), 3L));
    }

    @Test
    void shouldReturnEmptyMapForBlankXmlInCountInstances() {
        assertThat(scanner.countInstances(null)).isEmpty();
        assertThat(scanner.countInstances("")).isEmpty();
        assertThat(scanner.countInstances("   ")).isEmpty();
    }

    @Test
    void shouldRejectUnsafeXmlInCountInstances() {
        String xml = """
            <?xml version="1.0"?>
            <!DOCTYPE foo [
              <!ENTITY xxe SYSTEM "file:///etc/passwd">
            ]>
            <bpmn:definitions
                xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL">
              <bpmn:process id="Process_1">
                <bpmn:task id="Task_1" name="&xxe;"/>
              </bpmn:process>
            </bpmn:definitions>
            """;

        assertThatThrownBy(() -> scanner.countInstances(xml))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid or unsafe BPMN XML");
    }

    @Test
    void shouldUseNamespaceUriAndNotPrefixInScanInstances() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <bpmn:definitions
                xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                xmlns:x="FileReceiver">

              <bpmn:process id="Process_1">
                <x:fileReceiver id="FR_1"/>
              </bpmn:process>

            </bpmn:definitions>
            """;

        assertThat(scanner.scanInstances(xml))
            .containsExactly(Map.entry("FR_1", new XmlElementKey("FileReceiver", "fileReceiver")));
    }
}
