package com.behsa.medportal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 * Catalog entry for a BPMN element that may be exposed to portal owners through element groups.
 *
 * <p>The UI uses {@code bpmnType} (for example {@code FileReceiver:FileReceiver}) while the
 * server validates XML by namespace URI + local name. XML prefixes are intentionally not used
 * for validation because callers are free to rename a prefix without changing the XML meaning.
 */
@Entity
@Table(name = "TBL_BPMN_ELEMENT")
public class BpmnElementEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "BPMN_ELEMENT_SEQ_GENERATOR")
    @SequenceGenerator(name = "BPMN_ELEMENT_SEQ_GENERATOR", sequenceName = "BPMN_ELEMENTS_SEQ", allocationSize = 1)
    @Column(name = "element_key")
    private Long id;

    @Column(name = "element_code", length = 100, nullable = false, unique = true)
    private String elementCode;

    @Column(name = "bpmn_type", length = 150, nullable = false, unique = true)
    private String bpmnType;

    @Column(name = "namespace_uri", length = 300, nullable = false)
    private String namespaceUri;

    @Column(name = "local_name", length = 150, nullable = false)
    private String localName;

    @Column(name = "palette_action", length = 150)
    private String paletteAction;

    @Column(name = "display_name", length = 200, nullable = false)
    private String displayName;

    @Column(name = "enabled", nullable = false)
    private Integer enabled;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getElementCode() {
        return elementCode;
    }

    public void setElementCode(String elementCode) {
        this.elementCode = elementCode;
    }

    public String getBpmnType() {
        return bpmnType;
    }

    public void setBpmnType(String bpmnType) {
        this.bpmnType = bpmnType;
    }

    public String getNamespaceUri() {
        return namespaceUri;
    }

    public void setNamespaceUri(String namespaceUri) {
        this.namespaceUri = namespaceUri;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getPaletteAction() {
        return paletteAction;
    }

    public void setPaletteAction(String paletteAction) {
        this.paletteAction = paletteAction;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
