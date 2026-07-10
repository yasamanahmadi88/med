package com.behsa.medportal.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A ResourceEntity.
 */
@Entity
@Table(name = "jhi_resource")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ResourceEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "AUTR_SEQ_GENERATOR")
    @SequenceGenerator(name = "AUTR_SEQ_GENERATOR", sequenceName = "RESRC_SEQ", allocationSize = 0)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 200)
    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @NotNull
    @Size(max = 300)
    @Column(name = "display_name", length = 300, nullable = false)
    private String displayName;

    @NotNull
    @Size(max = 1000)
    @Column(name = "api_uri", length = 1000, nullable = false)
    private String apiUri;

    @NotNull
    @Size(max = 255)
    @Column(name = "resource_type", length = 255, nullable = false)
    private String resourceType;

    @OneToMany(mappedBy = "resource")
    @JsonIgnoreProperties(value = { "medAuthority", "resource" }, allowSetters = true)
    private Set<ResourceAuthorityEntity> resourceAuthorities = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public ResourceEntity id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public ResourceEntity name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public ResourceEntity displayName(String displayName) {
        this.setDisplayName(displayName);
        return this;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getApiUri() {
        return this.apiUri;
    }

    public ResourceEntity apiUri(String apiUri) {
        this.setApiUri(apiUri);
        return this;
    }

    public void setApiUri(String apiUri) {
        this.apiUri = apiUri;
    }

    public String getResourceType() {
        return this.resourceType;
    }

    public ResourceEntity resourceType(String resourceType) {
        this.setResourceType(resourceType);
        return this;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Set<ResourceAuthorityEntity> getResourceAuthorities() {
        return this.resourceAuthorities;
    }

    public void setResourceAuthorities(Set<ResourceAuthorityEntity> resourceAuthorities) {
        if (this.resourceAuthorities != null) {
            this.resourceAuthorities.forEach(i -> i.setResource(null));
        }
        if (resourceAuthorities != null) {
            resourceAuthorities.forEach(i -> i.setResource(this));
        }
        this.resourceAuthorities = resourceAuthorities;
    }

    public ResourceEntity resourceAuthorities(Set<ResourceAuthorityEntity> resourceAuthorities) {
        this.setResourceAuthorities(resourceAuthorities);
        return this;
    }

    public ResourceEntity addResourceAuthorities(ResourceAuthorityEntity resourceAuthority) {
        this.resourceAuthorities.add(resourceAuthority);
        resourceAuthority.setResource(this);
        return this;
    }

    public ResourceEntity removeResourceAuthorities(ResourceAuthorityEntity resourceAuthority) {
        this.resourceAuthorities.remove(resourceAuthority);
        resourceAuthority.setResource(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResourceEntity)) {
            return false;
        }
        return id != null && id.equals(((ResourceEntity) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ResourceEntity{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", displayName='" + getDisplayName() + "'" +
            ", apiUri='" + getApiUri() + "'" +
            ", resourceType='" + getResourceType() + "'" +
            "}";
    }
}
