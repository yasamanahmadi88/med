package com.behsa.medportal.service.criteria;

import java.io.Serializable;
import java.util.Objects;

import com.behsa.medportal.domain.FlowEntity;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link FlowEntity} entity. This class is used
 * in {@link com.behsa.medportal.web.rest.FlowResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /flows?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class FlowCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter flowName;

    private StringFilter flowDesc;

    private StringFilter flow;

    private LongFilter productId;

    private Boolean distinct;

    public FlowCriteria() {}

    public FlowCriteria(FlowCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.flowName = other.flowName == null ? null : other.flowName.copy();
        this.flowDesc = other.flowDesc == null ? null : other.flowDesc.copy();
        this.flow = other.flow == null ? null : other.flow.copy();
        this.productId = other.productId == null ? null : other.productId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public FlowCriteria copy() {
        return new FlowCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public LongFilter id() {
        if (id == null) {
            id = new LongFilter();
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getFlowName() {
        return flowName;
    }

    public StringFilter flowName() {
        if (flowName == null) {
            flowName = new StringFilter();
        }
        return flowName;
    }

    public void setFlowName(StringFilter flowName) {
        this.flowName = flowName;
    }

    public StringFilter getFlowDesc() {
        return flowDesc;
    }

    public StringFilter flowDesc() {
        if (flowDesc == null) {
            flowDesc = new StringFilter();
        }
        return flowDesc;
    }

    public void setFlowDesc(StringFilter flowDesc) {
        this.flowDesc = flowDesc;
    }

    public StringFilter getFlow() {
        return flow;
    }

    public StringFilter flow() {
        if (flow == null) {
            flow = new StringFilter();
        }
        return flow;
    }

    public void setFlow(StringFilter flow) {
        this.flow = flow;
    }

    public LongFilter getProductId() {
        return productId;
    }

    public LongFilter productId() {
        if (productId == null) {
            productId = new LongFilter();
        }
        return productId;
    }

    public void setProductId(LongFilter productId) {
        this.productId = productId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FlowCriteria that = (FlowCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(flowName, that.flowName) &&
            Objects.equals(flowDesc, that.flowDesc) &&
            Objects.equals(flow, that.flow) &&
            Objects.equals(productId, that.productId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, flowName, flowDesc, flow, productId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "FlowCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (flowName != null ? "flowName=" + flowName + ", " : "") +
            (flowDesc != null ? "flowDesc=" + flowDesc + ", " : "") +
            (flow != null ? "flow=" + flow + ", " : "") +
            (productId != null ? "productId=" + productId + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
