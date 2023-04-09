package com.behsa.medportal.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A ResourceAuthorityEntity.
 */
@Entity
@Table(name = "jhi_resource_authority")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ResourceAuthorityEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "AUTR_SEQ_GENERATOR")
    @SequenceGenerator(name = "AUTR_SEQ_GENERATOR", sequenceName = "RES_AUTH_SEQ", allocationSize = 0)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 255)
    @Column(name = "verb", length = 255, nullable = false)
    private String verb;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "resourceAuthorities" }, allowSetters = true)
    @JoinColumn(name = "authority_id", referencedColumnName = "id")
    private MedAuthorityEntity medAuthority;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "resourceAuthorities" }, allowSetters = true)
    @JoinColumn(name = "resource_id", referencedColumnName = "id")
    private ResourceEntity resource;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public ResourceAuthorityEntity id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVerb() {
        return this.verb;
    }

    public ResourceAuthorityEntity verb(String verb) {
        this.setVerb(verb);
        return this;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public MedAuthorityEntity getMedAuthority() {
        return this.medAuthority;
    }

    public void setMedAuthority(MedAuthorityEntity medAuthority) {
        this.medAuthority = medAuthority;
    }

    public ResourceAuthorityEntity medAuthority(MedAuthorityEntity medAuthority) {
        this.setMedAuthority(medAuthority);
        return this;
    }

    public ResourceEntity getResource() {
        return this.resource;
    }

    public void setResource(ResourceEntity resource) {
        this.resource = resource;
    }

    public ResourceAuthorityEntity resource(ResourceEntity resource) {
        this.setResource(resource);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResourceAuthorityEntity)) {
            return false;
        }
        return id != null && id.equals(((ResourceAuthorityEntity) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ResourceAuthorityEntity{" +
            "id=" + getId() +
            ", verb='" + getVerb() + "'" +
            "}";
    }
}
