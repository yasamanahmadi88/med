package com.behsa.medportal.service;

import com.behsa.medportal.domain.*;
import com.behsa.medportal.repository.FlowRepository;
import com.behsa.medportal.service.criteria.FlowCriteria;
import com.behsa.medportal.service.dto.FlowDTO;
import com.behsa.medportal.service.mapper.FlowMapper;
import java.util.List;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link FlowEntity} entities in the database.
 * The main input is a {@link FlowCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link FlowDTO} or a {@link Page} of {@link FlowDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class FlowQueryService extends QueryService<FlowEntity> {

    private final Logger log = LoggerFactory.getLogger(FlowQueryService.class);

    private final FlowRepository flowRepository;

    private final FlowMapper flowMapper;

    public FlowQueryService(FlowRepository flowRepository, FlowMapper flowMapper) {
        this.flowRepository = flowRepository;
        this.flowMapper = flowMapper;
    }

    /**
     * Return a {@link List} of {@link FlowDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<FlowDTO> findByCriteria(FlowCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<FlowEntity> specification = createSpecification(criteria);
        return flowMapper.toDto(flowRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link FlowDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<FlowDTO> findByCriteria(FlowCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<FlowEntity> specification = createSpecification(criteria);
        return flowRepository.findAll(specification, page).map(flowMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(FlowCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<FlowEntity> specification = createSpecification(criteria);
        return flowRepository.count(specification);
    }

    /**
     * Function to convert {@link FlowCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<FlowEntity> createSpecification(FlowCriteria criteria) {
        Specification<FlowEntity> specification = (root, query, criteriaBuilder) -> null;
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), FlowEntity_.id));
            }
            if (criteria.getFlowName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getFlowName(), FlowEntity_.flowName));
            }
            if (criteria.getFlowDesc() != null) {
                specification = specification.and(buildStringSpecification(criteria.getFlowDesc(), FlowEntity_.flowDesc));
            }
            if (criteria.getFlow() != null) {
                specification = specification.and(buildStringSpecification(criteria.getFlow(), FlowEntity_.flow));
            }
            if (criteria.getProductId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getProductId(),
                            root -> root.join(FlowEntity_.product, JoinType.LEFT).get(ProductEntity_.id)
                        )
                    );
            }
        }
        return specification;
    }
}
