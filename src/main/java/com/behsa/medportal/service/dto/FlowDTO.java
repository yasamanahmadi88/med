package com.behsa.medportal.service.dto;

import com.behsa.medportal.domain.FlowEntity;

import java.io.Serializable;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link FlowEntity} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class FlowDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 6)
    private String flowName;

    @NotNull
    @Size(max = 300)
    private String flowDesc;

    @NotNull
    private String flow;

    private ProductDTO product;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getFlowDesc() {
        return flowDesc;
    }

    public void setFlowDesc(String flowDesc) {
        this.flowDesc = flowDesc;
    }

    public String getFlow() {
        return flow;
    }

    public void setFlow(String flow) {
        this.flow = flow;
    }

    public ProductDTO getProduct() {
        return product;
    }

    public void setProduct(ProductDTO product) {
        this.product = product;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FlowDTO)) {
            return false;
        }

        FlowDTO flowDTO = (FlowDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, flowDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "FlowDTO{" +
            "id=" + getId() +
            ", flowName='" + getFlowName() + "'" +
            ", flowDesc='" + getFlowDesc() + "'" +
            ", flow='" + getFlow() + "'" +
            ", product=" + getProduct() +
            "}";
    }
}
