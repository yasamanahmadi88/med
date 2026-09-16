package com.behsa.medportal.service.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Product-specific BPMN element configuration consumed by the Angular editor. */
public class BpmnElementAccessDTO implements Serializable {

    private Long productId;
    private String productName;
    private List<GroupDTO> groups = new ArrayList<>();
    private List<ElementDTO> elements = new ArrayList<>();

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public List<GroupDTO> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupDTO> groups) {
        this.groups = groups;
    }

    public List<ElementDTO> getElements() {
        return elements;
    }

    public void setElements(List<ElementDTO> elements) {
        this.elements = elements;
    }

    public record GroupDTO(Long id, String code, String name) implements Serializable {}

    public record ElementDTO(
        Long id,
        String code,
        String bpmnType,
        String namespaceUri,
        String localName,
        String paletteAction,
        String displayName,
        Integer sortOrder
    ) implements Serializable {}
}
