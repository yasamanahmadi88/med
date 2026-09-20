package com.behsa.medportal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.io.Serializable;

/** A reusable group of BPMN elements that can be assigned to a portal owner. */
@Entity
@Table(name = "TBL_BPMN_ELEMENT_GROUP")
public class BpmnElementGroupEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "BPMN_ELEMENT_GROUP_SEQ_GENERATOR")
    @SequenceGenerator(name = "BPMN_ELEMENT_GROUP_SEQ_GENERATOR", sequenceName = "BPMN_ELEMENT_GROUPS_SEQ", allocationSize = 1)
    @Column(name = "group_key")
    private Long id;

    @Column(name = "group_code", length = 100, nullable = false, unique = true)
    private String groupCode;

    @Column(name = "group_name", length = 200, nullable = false)
    private String groupName;

    @Column(name = "enabled", nullable = false)
    private Integer enabled;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }
}
