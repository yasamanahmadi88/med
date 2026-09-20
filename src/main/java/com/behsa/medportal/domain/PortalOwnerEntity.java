package com.behsa.medportal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "TBL_PORTAL_OWNER")
public class PortalOwnerEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "PORTAL_OWNER_SEQ_GENERATOR")
    @SequenceGenerator(
        name = "PORTAL_OWNER_SEQ_GENERATOR",
        sequenceName = "PORTAL_OWNER_SEQ",
        allocationSize = 1
    )
    @Column(name = "owner_key")
    private Long id;

    @Column(name = "owner_code", length = 50, nullable = false, unique = true)
    private String ownerCode;

    @Column(name = "owner_name", length = 100, nullable = false)
    private String ownerName;

    @Column(name = "display_name", length = 150, nullable = false)
    private String displayName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "enabled", nullable = false)
    private Integer enabled;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOwnerCode() {
        return ownerCode;
    }

    public void setOwnerCode(String ownerCode) {
        this.ownerCode = ownerCode;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }
}