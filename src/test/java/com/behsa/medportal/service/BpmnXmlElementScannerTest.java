package com.behsa.medportal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.behsa.medportal.service.BpmnXmlElementScanner.XmlElementKey;
import org.junit.jupiter.api.Test;

class BpmnXmlElementScannerTest {

    private final BpmnXmlElementScanner scanner = new BpmnXmlElementScanner();

    @Test
    void scansInstancesByNamespaceLocalNameAndId() {
        String xml = wrap("<legacy:cdrParser id=\"CDR_1\" name=\"editable\" />");

        assertThat(scanner.scanInstances(xml)).containsEntry("CDR_1", new XmlElementKey("CdrParser", "cdrParser"));
    }

    @Test
    void rejectsDuplicateAndMissingAccessControlledIds() {
        assertThatThrownBy(() -> scanner.scanInstances(wrap("<legacy:cdrParser id=\"same\"/><legacy:cdrParser id=\"same\"/>")))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> scanner.scanInstances(wrap("<legacy:cdrParser/>")))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsDoctype() {
        assertThatThrownBy(() -> scanner.scanInstances("<!DOCTYPE x><x/>"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private String wrap(String body) {
        return "<bpmn:definitions xmlns:bpmn=\"" + BpmnXmlElementScanner.BPMN_MODEL_NS +
            "\" xmlns:legacy=\"CdrParser\"><bpmn:process id=\"Process_1\">" + body +
            "</bpmn:process></bpmn:definitions>";
    }
}
