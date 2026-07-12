package com.behsa.medportal.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * A ProductEntity.
 */
@Entity
@Table(name = "TBL_PRODUCTS")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ProductEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "AUTR_SEQ_GENERATOR")
    @SequenceGenerator(name = "AUTR_SEQ_GENERATOR", sequenceName = "PRODUCTS_SEQ", allocationSize = 1)
    @Column(name = "product_key")
    private Long id;

    @NotNull
    @Size(max = 3)
    @Column(name = "product_name", length = 3, nullable = false)
    private String productName;

    @NotNull
    @Size(max = 300)
    @Column(name = "product_desc", length = 300, nullable = false)
    private String productDesc;

    @OneToMany(mappedBy = "product")
    @JsonIgnoreProperties(value = { "product" }, allowSetters = true)
    private Set<FlowEntity> flows = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public ProductEntity id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductName() {
        return this.productName;
    }

    public ProductEntity productName(String productName) {
        this.setProductName(productName);
        return this;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDesc() {
        return this.productDesc;
    }

    public ProductEntity productDesc(String productDesc) {
        this.setProductDesc(productDesc);
        return this;
    }

    public void setProductDesc(String productDesc) {
        this.productDesc = productDesc;
    }

    public Set<FlowEntity> getFlows() {
        return this.flows;
    }

    public void setFlows(Set<FlowEntity> flows) {
        if (this.flows != null) {
            this.flows.forEach(i -> i.setProduct(null));
        }
        if (flows != null) {
            flows.forEach(i -> i.setProduct(this));
        }
        this.flows = flows;
    }

    public ProductEntity flows(Set<FlowEntity> flows) {
        this.setFlows(flows);
        return this;
    }

    public ProductEntity addFlows(FlowEntity flow) {
        this.flows.add(flow);
        flow.setProduct(this);
        return this;
    }

    public ProductEntity removeFlows(FlowEntity flow) {
        this.flows.remove(flow);
        flow.setProduct(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProductEntity)) {
            return false;
        }
        return id != null && id.equals(((ProductEntity) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ProductEntity{" +
            "id=" + getId() +
            ", productName='" + getProductName() + "'" +
            ", productDesc='" + getProductDesc() + "'" +
            "}";
    }
}
