package com.behsa.medportal.service.dto;

import java.io.Serializable;
import java.util.Objects;
import jakarta.validation.constraints.*;

/**
 * A DTO for the {@link com.behsa.medportal.domain.MedAuthorityEntity} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MedAuthorityDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 50)
    private String name;

    @Size(max = 500)
    private String displayName;

    private Long parentId;

    private String parentDisplayName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getParentDisplayName() {
        return parentDisplayName;
    }

    public MedAuthorityDTO setParentDisplayName(String parentDisplayName) {
        this.parentDisplayName = parentDisplayName;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MedAuthorityDTO)) {
            return false;
        }

        MedAuthorityDTO medAuthorityDTO = (MedAuthorityDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, medAuthorityDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MedAuthorityDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", displayName='" + getDisplayName() + "'" +
            ", parentId=" + getParentId() +
            "}";
    }
}
