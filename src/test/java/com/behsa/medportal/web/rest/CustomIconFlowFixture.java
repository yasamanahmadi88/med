package com.behsa.medportal.web.rest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Builds a BPMN document of the shape the editor now produces, so that tests store something a
 * real flow could be rather than a ten-character placeholder.
 *
 * <p>The custom-icon feature keeps its icon library inside the diagram's own XML
 * (see {@code src/main/webapp/app/bpmn-editor/README.md}, "Custom icons"): a
 * {@code customIcon:iconLibrary} under {@code bpmn:extensionElements}, one
 * {@code customIcon:icon} per icon carrying its SVG as a base64 {@code data:} URI, and
 * {@code customIcon:customTask} shapes referring to them by id. Both caps the editor enforces are
 * respected here — 32 KB per icon on the SVG source, 192 KB per diagram on the stored
 * {@code data:} URIs — so this fixture sits at the largest flow the client can produce.
 *
 * <p>The process name is Persian on purpose. {@code flow} is a CLOB in the production schema, and a
 * database or driver that quietly narrowed it to a single-byte character set would lose those
 * characters while every ASCII assertion still passed.
 */
public final class CustomIconFlowFixture {

    /** The per-diagram cap {@code CustomIconLibrary.add} enforces, in characters of stored data: URI. */
    public static final int DIAGRAM_ICON_BUDGET = 192 * 1024;

    /** The per-icon cap {@code CustomIconLibrary.add} enforces, in UTF-8 bytes of SVG source. */
    public static final int ICON_SOURCE_CAP = 32 * 1024;

    private static final int ICON_COUNT = 6;

    private static final String DATA_URI_PREFIX = "data:image/svg+xml;base64,";

    /**
     * SVG source length per icon, in bytes. Chosen so that the six data: URIs together come to
     * 196 596 characters — just inside the 192 KB diagram budget — and each icon stays well inside
     * the 32 KB per-icon cap. Divisible by three, so base64 adds no padding and the arithmetic in
     * {@link #assertWithinEditorCaps} is exact.
     */
    private static final int ICON_SOURCE_BYTES = 24_555;

    /** Distinct diagram ids, so that one fixture can be selected by flow.contains and the other rejected. */
    public static final String CREATE_PROCESS_ID = "Process_1788748197978";

    public static final String UPDATE_PROCESS_ID = "Process_1788748197979";

    /**
     * The id of the placed {@code customIcon:customTask}, and the needle the {@code flow.contains}
     * tests search for. It is deliberately not the process id: the process id is repeated in the
     * {@code bpmn:definitions} id at the top of the document, where a LIKE would find it in the
     * first hundred characters. The task id first appears after the whole icon library, so matching
     * it means the database really did search past 192 KB of CLOB.
     */
    public static final String CREATE_TASK_ID = "Activity_0ip6hnb";

    public static final String UPDATE_TASK_ID = "Activity_1qm4tzc";

    /** A Persian process name: the round trip has to carry characters outside Latin-1. */
    public static final String PERSIAN_PROCESS_NAME = "فرآیند پرداخت";

    private static final String PERSIAN_TASK_NAME = "پرداخت";

    private CustomIconFlowFixture() {}

    /**
     * A complete diagram carrying a full icon library. Two fixtures built with different
     * {@code processId}/{@code taskId} pairs differ in a way {@code flow.contains} can tell apart.
     */
    public static String flowWithIconLibrary(String processId, String taskId) {
        StringBuilder xml = new StringBuilder(DIAGRAM_ICON_BUDGET + 4096);
        xml
            .append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            .append("<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"")
            .append(" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\"")
            .append(" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\"")
            .append(" xmlns:customIcon=\"http://medportal.behsa.com/schema/bpmn/custom-icons\"")
            .append(" id=\"Definitions_")
            .append(processId)
            .append("\" targetNamespace=\"http://bpmn.io/schema/bpmn\">\n")
            .append("  <bpmn:extensionElements>\n")
            .append("    <customIcon:iconLibrary>\n");

        for (int i = 1; i <= ICON_COUNT; i++) {
            xml
                .append("      <customIcon:icon iconId=\"Icon_")
                .append(i)
                .append("\" name=\"Payment ")
                .append(i)
                .append("\" contents=\"")
                .append(iconDataUri(i))
                .append("\" />\n");
        }

        xml
            .append("    </customIcon:iconLibrary>\n")
            .append("  </bpmn:extensionElements>\n")
            .append("  <bpmn:process id=\"")
            .append(processId)
            .append("\" name=\"")
            .append(PERSIAN_PROCESS_NAME)
            .append("\" isExecutable=\"true\">\n")
            .append("    <bpmn:startEvent id=\"StartEvent_1\" />\n")
            .append("    <customIcon:customTask id=\"")
            .append(taskId)
            .append("\" name=\"")
            .append(PERSIAN_TASK_NAME)
            .append("\" iconId=\"Icon_1\" />\n")
            .append("    <bpmn:endEvent id=\"EndEvent_1\" />\n")
            .append("  </bpmn:process>\n")
            .append("  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n")
            .append("    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"")
            .append(processId)
            .append("\">\n")
            .append("      <bpmndi:BPMNShape id=\"")
            .append(taskId)
            .append("_di\" bpmnElement=\"")
            .append(taskId)
            .append("\">\n")
            .append("        <dc:Bounds x=\"400\" y=\"260\" width=\"120\" height=\"120\" />\n")
            .append("      </bpmndi:BPMNShape>\n")
            .append("    </bpmndi:BPMNPlane>\n")
            .append("  </bpmndi:BPMNDiagram>\n")
            .append("</bpmn:definitions>\n");

        return xml.toString();
    }

    /**
     * Fails if the fixture has drifted outside the caps the editor enforces. A fixture larger than
     * the client can produce would be testing a case that cannot happen; one much smaller would stop
     * being a CLOB test. Called from the tests so a bad edit here is reported there.
     */
    public static void assertWithinEditorCaps() {
        int storedIconCharacters = 0;
        for (int i = 1; i <= ICON_COUNT; i++) {
            String svg = iconSource(i);
            int sourceBytes = svg.getBytes(StandardCharsets.UTF_8).length;
            if (sourceBytes > ICON_SOURCE_CAP) {
                throw new IllegalStateException("icon " + i + " is " + sourceBytes + " bytes, over the 32 KB per-icon cap");
            }
            storedIconCharacters += iconDataUri(i).length();
        }
        if (storedIconCharacters > DIAGRAM_ICON_BUDGET) {
            throw new IllegalStateException(
                "the icon library is " + storedIconCharacters + " characters, over the 192 KB per-diagram cap"
            );
        }
        // Below half the budget the fixture would no longer be the large value this file exists to test.
        if (storedIconCharacters < DIAGRAM_ICON_BUDGET / 2) {
            throw new IllegalStateException("the icon library shrank to " + storedIconCharacters + " characters");
        }
    }

    private static String iconDataUri(int index) {
        return DATA_URI_PREFIX + Base64.getEncoder().encodeToString(iconSource(index).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * A real, if dull, SVG padded out inside an XML comment to {@link #ICON_SOURCE_BYTES}. Padding
     * rather than random bytes keeps the fixture identical from run to run, which is what lets the
     * round-trip assertions compare the whole document.
     */
    private static String iconSource(int index) {
        String head =
            "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 24 24\"><title>Payment " +
            index +
            "</title><path d=\"M4 4h16v16H4z\"/><!--";
        String tail = "--></svg>";
        int padding = ICON_SOURCE_BYTES - head.length() - tail.length();
        if (padding < 0) {
            throw new IllegalStateException("ICON_SOURCE_BYTES is too small for the icon markup");
        }
        return head + ".".repeat(padding) + tail;
    }
}
