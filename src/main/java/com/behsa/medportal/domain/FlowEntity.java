package com.behsa.medportal.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * A FlowEntity.
 */
@Entity
@Table(name = "TBL_FLOWS")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class FlowEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "AUTR_SEQ_GENERATOR")
    @SequenceGenerator(name = "AUTR_SEQ_GENERATOR", sequenceName = "FLOWS_SEQ", allocationSize = 1)
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

    // The BPMN diagram XML, which since the custom-icon feature also carries the diagram's icon
    // library as base64 data: URIs — up to 192 KB of it. Liquibase owns the production schema and
    // declares this column ${clobType}, and ddl-auto is none in production, so what is written here
    // shapes only the schemas Hibernate generates. That is exactly why it has to agree with
    // Liquibase: left at the default length it made every generated schema varchar(255), so neither
    // the H2 suite nor the oracle-testcontainers suite could store a real flow, and the CLOB the
    // application actually runs against went untested. FlowResourceIT covers it now.
    //
    // columnDefinition rather than length or @Lob, and the difference matters. Widening the length
    // past varchar (or adding @Lob) changes the *mapping*, and JHipster's StringFilter support then
    // stops compiling a query at all:
    //
    //   FunctionArgumentException: Parameter 1 of function 'upper()' has type 'STRING',
    //   but argument is of type 'java.lang.String' mapped to 'CLOB'
    //
    // — from Hibernate, on every database, which takes flow.contains with it. columnDefinition
    // changes only the generated DDL; the attribute stays an ordinary String bound as a varchar,
    // which is precisely the arrangement production has always had (String over ${clobType}).
    // Only testdev (H2, create-drop) and testcontainers (Oracle, update) generate DDL, and both
    // accept "clob"; every other profile runs ddl-auto: none. A future profile generating DDL on
    // PostgreSQL would not — "clob" is not a PostgreSQL type — and would need a dialect-aware column.
    //
    // The consequence of the column being a CLOB, measured on Oracle Free 23.26: = and IN against it
    // fail with "ORA-22848: cannot use CLOB type as comparison key", at any value length, while H2
    // accepts both. FlowResource therefore refuses flow.equals/flow.notEquals/flow.in/flow.notIn.
    // LIKE is fine against a LOB on both, so flow.contains and flow.doesNotContain are unaffected.
    @NotNull
    @Column(name = "flow", nullable = false, columnDefinition = "clob")
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
