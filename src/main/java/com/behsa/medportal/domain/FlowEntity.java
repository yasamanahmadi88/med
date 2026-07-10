package com.behsa.medportal.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A FlowEntity.
 */
@Entity
@Table(name = "MEDIATION.TBL_FLOWS")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class FlowEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "AUTR_SEQ_GENERATOR")
    @SequenceGenerator(name = "AUTR_SEQ_GENERATOR", sequenceName = "MEDIATION.FLOWS_SEQ", allocationSize = 0)
    @Column(name = "flow_key")
    private Long id;

    @NotNull
    @Size(max = 6)
    @Column(name = "flow_name", length = 6, nullable = false)
    private String flowName;

    @NotNull
    @Size(max = 300)
    @Column(name = "flow_desc", length = 300, nullable = false)
    private String flowDesc;

    @NotNull
    @Column(name = "flow", nullable = false)
    private String flow;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "flows" }, allowSetters = true)
    @JoinColumn(name = "product_name", referencedColumnName = "product_name")
    private ProductEntity product;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public FlowEntity id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFlowName() {
        return this.flowName;
    }

    public FlowEntity flowName(String flowName) {
        this.setFlowName(flowName);
        return this;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getFlowDesc() {
        return this.flowDesc;
    }

    public FlowEntity flowDesc(String flowDesc) {
        this.setFlowDesc(flowDesc);
        return this;
    }

    public void setFlowDesc(String flowDesc) {
        this.flowDesc = flowDesc;
    }

    public String getFlow() {
        return this.flow;
    }

    public FlowEntity flow(String flow) {
        this.setFlow(flow);
        return this;
    }

    public void setFlow(String flow) {
        this.flow = flow;
    }

    public ProductEntity getProduct() {
        return this.product;
    }

    public void setProduct(ProductEntity product) {
        this.product = product;
    }

    public FlowEntity product(ProductEntity product) {
        this.setProduct(product);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FlowEntity)) {
            return false;
        }
        return id != null && id.equals(((FlowEntity) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "FlowEntity{" +
            "id=" + getId() +
            ", flowName='" + getFlowName() + "'" +
            ", flowDesc='" + getFlowDesc() + "'" +
            ", flow='" + getFlow() + "'" +
            "}";
    }
}
