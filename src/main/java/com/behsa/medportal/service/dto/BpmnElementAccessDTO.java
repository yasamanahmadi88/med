package com.behsa.medportal.service.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * BPMN capability configuration for the active portal Owner.
 *
 * Product and user role are deliberately absent from this authorization model.
 */
public class BpmnElementAccessDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String ownerCode;
    private String ownerDisplayName;
    private List<GroupDTO> groups = new ArrayList<>();
    private List<ElementDTO> elements = new ArrayList<>();

    public String getOwnerCode() {
        return ownerCode;
    }

    public void setOwnerCode(String ownerCode) {
        this.ownerCode = ownerCode;
    }

    public String getOwnerDisplayName() {
        return ownerDisplayName;
    }

    public void setOwnerDisplayName(String ownerDisplayName) {
        this.ownerDisplayName = ownerDisplayName;
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