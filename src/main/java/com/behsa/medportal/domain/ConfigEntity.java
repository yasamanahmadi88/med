package com.behsa.medportal.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * A ConfigEntity.
 */
@Entity
@Table(name = "MEDIATION.tbl_configs")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ConfigEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "AUTR_SEQ_GENERATOR")
    @SequenceGenerator(name = "AUTR_SEQ_GENERATOR", sequenceName = "CONFIGS_SEQ", allocationSize = 0)
    @Column(name = "config_key")
    private Long id;

    @NotNull
    @Size(max = 100)
    @Column(name = "property", length = 100, nullable = false)
    private String property;

    @NotNull
    @Size(max = 300)
    @Column(name = "p_value", length = 300, nullable = false)
    private String pValue;

    @Size(max = 200)
    @Column(name = "comment_desc", length = 200)
    private String commentDesc;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "configs" }, allowSetters = true)
    @JoinColumn(name = "module_name", referencedColumnName = "module_name")
    private ModuleEntity module;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public ConfigEntity id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProperty() {
        return this.property;
    }

    public ConfigEntity property(String property) {
        this.setProperty(property);
        return this;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getpValue() {
        return this.pValue;
    }

    public ConfigEntity pValue(String pValue) {
        this.setpValue(pValue);
        return this;
    }

    public void setpValue(String pValue) {
        this.pValue = pValue;
    }

    public String getCommentDesc() {
        return this.commentDesc;
    }

    public ConfigEntity commentDesc(String commentDesc) {
        this.setCommentDesc(commentDesc);
        return this;
    }

    public void setCommentDesc(String commentDesc) {
        this.commentDesc = commentDesc;
    }

    public ModuleEntity getModule() {
        return this.module;
    }

    public void setModule(ModuleEntity module) {
        this.module = module;
    }

    public ConfigEntity module(ModuleEntity module) {
        this.setModule(module);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConfigEntity)) {
            return false;
        }
        return id != null && id.equals(((ConfigEntity) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ConfigEntity{" +
            "id=" + getId() +
            ", property='" + getProperty() + "'" +
            ", pValue='" + getpValue() + "'" +
            ", commentDesc='" + getCommentDesc() + "'" +
            "}";
    }
}
