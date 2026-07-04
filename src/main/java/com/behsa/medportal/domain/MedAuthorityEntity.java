package com.behsa.medportal.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * A MedAuthorityEntity.
 */
@Entity
@Table(name = "jhi_authority")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MedAuthorityEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "AUTR_SEQ_GENERATOR")
    @SequenceGenerator(name = "AUTR_SEQ_GENERATOR", sequenceName = "AUTH_SEQ", allocationSize = 0)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 50)
    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Size(max = 500)
    @Column(name = "display_name", length = 500)
    private String displayName;

    @ManyToOne
    @JoinColumn(name = "parent_id", referencedColumnName = "ID")
    private MedAuthorityEntity parent;

    @OneToMany(mappedBy = "medAuthority")
    @JsonIgnoreProperties(value = { "medAuthority", "resource" }, allowSetters = true)
    private Set<ResourceAuthorityEntity> resourceAuthorities = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public MedAuthorityEntity id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public MedAuthorityEntity name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public MedAuthorityEntity displayName(String displayName) {
        this.setDisplayName(displayName);
        return this;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public MedAuthorityEntity getParent() {
        return parent;
    }

    public MedAuthorityEntity setParent(MedAuthorityEntity parent) {
        this.parent = parent;
        return this;
    }

    public Set<ResourceAuthorityEntity> getResourceAuthorities() {
        return this.resourceAuthorities;
    }

    public void setResourceAuthorities(Set<ResourceAuthorityEntity> resourceAuthorities) {
        if (this.resourceAuthorities != null) {
            this.resourceAuthorities.forEach(i -> i.setMedAuthority(null));
        }
        if (resourceAuthorities != null) {
            resourceAuthorities.forEach(i -> i.setMedAuthority(this));
        }
        this.resourceAuthorities = resourceAuthorities;
    }

    public MedAuthorityEntity resourceAuthorities(Set<ResourceAuthorityEntity> resourceAuthorities) {
        this.setResourceAuthorities(resourceAuthorities);
        return this;
    }

    public MedAuthorityEntity addResourceAuthorities(ResourceAuthorityEntity resourceAuthority) {
        this.resourceAuthorities.add(resourceAuthority);
        resourceAuthority.setMedAuthority(this);
        return this;
    }

    public MedAuthorityEntity removeResourceAuthorities(ResourceAuthorityEntity resourceAuthority) {
        this.resourceAuthorities.remove(resourceAuthority);
        resourceAuthority.setMedAuthority(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MedAuthorityEntity)) {
            return false;
        }
        return id != null && id.equals(((MedAuthorityEntity) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MedAuthorityEntity{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", displayName='" + getDisplayName() + "'" +
            ", parent=" + getParent() +
            "}";
    }
}
