package com.behsa.medportal.service;

import java.io.Reader;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Extracts user-modelled flow elements from BPMN XML using namespace-aware XML parsing.
 *
 * <p>Only element children of a BPMN Process/SubProcess (plus Participant inside a Collaboration)
 * are access-controlled. Technical XML such as sequenceFlow, extensionElements, DI and event
 * definitions is not a palette element and is therefore not treated as a permission target.
 */
@Component
public class BpmnXmlElementScanner {

    public static final String BPMN_MODEL_NS = "http://www.omg.org/spec/BPMN/20100524/MODEL";

    private static final Set<String> STRUCTURAL_FLOW_CHILDREN = Set.of(
        "sequenceFlow",
        "laneSet",
        "documentation",
        "extensionElements"
    );

    public Set<XmlElementKey> scan(String xml) {
        if (xml == null || xml.isBlank()) {
            return Set.of();
        }

        try {
            DocumentBuilderFactory factory = secureDocumentBuilderFactory();
            Document document = factory.newDocumentBuilder().parse(new InputSource(Reader.of(xml)));
            Set<XmlElementKey> result = new LinkedHashSet<>();
            collect(document.getDocumentElement(), result);
            return result;
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid or unsafe BPMN XML", exception);
        }
    }

    private void collect(Element element, Set<XmlElementKey> result) {
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (!(node instanceof Element child)) {
                continue;
            }

            if (isAccessControlledChild(element, child)) {
                result.add(new XmlElementKey(normalize(child.getNamespaceURI()), localName(child)));
            }

            collect(child, result);
        }
    }

    private boolean isAccessControlledChild(Element parent, Element child) {
        String parentNamespace = normalize(parent.getNamespaceURI());
        String parentLocalName = localName(parent);
        String childNamespace = normalize(child.getNamespaceURI());
        String childLocalName = localName(child);

        if (BPMN_MODEL_NS.equals(parentNamespace) && ("process".equals(parentLocalName) || "subProcess".equals(parentLocalName))) {
            return !(BPMN_MODEL_NS.equals(childNamespace) && STRUCTURAL_FLOW_CHILDREN.contains(childLocalName));
        }

        return BPMN_MODEL_NS.equals(parentNamespace)
            && "collaboration".equals(parentLocalName)
            && BPMN_MODEL_NS.equals(childNamespace)
            && "participant".equals(childLocalName);
    }

    private DocumentBuilderFactory secureDocumentBuilderFactory() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory;
    }

    private String localName(Element element) {
        return element.getLocalName() != null ? element.getLocalName() : element.getTagName();
    }

    private String normalize(String value) {
        return value == null ? "" : value;
    }

    public record XmlElementKey(String namespaceUri, String localName) {}
}
