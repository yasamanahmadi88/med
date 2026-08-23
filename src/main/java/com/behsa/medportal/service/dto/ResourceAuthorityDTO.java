package com.behsa.medportal.service.dto;

import com.behsa.medportal.domain.enumeration.Verb;

import java.io.Serializable;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link com.behsa.medportal.domain.ResourceAuthorityEntity} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ResourceAuthorityDTO implements Serializable {

    private Long id;

    @NotNull
    private Verb verb;

    private MedAuthorityDTO medAuthority;

    private ResourceDTO resource;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Verb getVerb() {
        return verb;
    }

    public void setVerb(Verb verb) {
        this.verb = verb;
    }

    public MedAuthorityDTO getMedAuthority() {
        return medAuthority;
    }

    public void setMedAuthority(MedAuthorityDTO medAuthority) {
        this.medAuthority = medAuthority;
    }

    public ResourceDTO getResource() {
        return resource;
    }

    public void setResource(ResourceDTO resource) {
        this.resource = resource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResourceAuthorityDTO)) {
            return false;
        }

        ResourceAuthorityDTO resourceAuthorityDTO = (ResourceAuthorityDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, resourceAuthorityDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ResourceAuthorityDTO{" +
            "id=" + getId() +
            ", verb='" + getVerb() + "'" +
            ", medAuthority=" + getMedAuthority() +
            ", resource=" + getResource() +
            "}";
    }
}
