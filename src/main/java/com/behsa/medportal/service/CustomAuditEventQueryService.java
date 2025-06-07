package com.behsa.medportal.service;

import com.behsa.medportal.domain.*; // for static metamodels
import com.behsa.medportal.domain.CustomAuditEventEntity;
import com.behsa.medportal.repository.CustomAuditEventRepository;
import com.behsa.medportal.service.criteria.CustomAuditEventCriteria;
import com.behsa.medportal.service.dto.CustomAuditEventDTO;
import com.behsa.medportal.service.mapper.CustomAuditEventMapper;
import java.util.List;
import javax.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link CustomAuditEventEntity} entities in the database.
 * The main input is a {@link CustomAuditEventCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link CustomAuditEventDTO} or a {@link Page} of {@link CustomAuditEventDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CustomAuditEventQueryService extends QueryService<CustomAuditEventEntity> {

    private final Logger log = LoggerFactory.getLogger(CustomAuditEventQueryService.class);

    private final CustomAuditEventRepository customAuditEventRepository;

    private final CustomAuditEventMapper customAuditEventMapper;

    public CustomAuditEventQueryService(
        CustomAuditEventRepository customAuditEventRepository,
        CustomAuditEventMapper customAuditEventMapper
    ) {
        this.customAuditEventRepository = customAuditEventRepository;
        this.customAuditEventMapper = customAuditEventMapper;
    }

    /**
     * Return a {@link List} of {@link CustomAuditEventDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<CustomAuditEventDTO> findByCriteria(CustomAuditEventCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<CustomAuditEventEntity> specification = createSpecification(criteria);
        return customAuditEventMapper.toDto(customAuditEventRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link CustomAuditEventDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CustomAuditEventDTO> findByCriteria(CustomAuditEventCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<CustomAuditEventEntity> specification = createSpecification(criteria);
        return customAuditEventRepository.findAll(specification, page).map(customAuditEventMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CustomAuditEventCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<CustomAuditEventEntity> specification = createSpecification(criteria);
        return customAuditEventRepository.count(specification);
    }

    /**
     * Function to convert {@link CustomAuditEventCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<CustomAuditEventEntity> createSpecification(CustomAuditEventCriteria criteria) {
        Specification<CustomAuditEventEntity> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), CustomAuditEventEntity_.id));
            }
            if (criteria.getPrincipal() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPrincipal(), CustomAuditEventEntity_.principal));
            }
            if (criteria.getEventDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getEventDate(), CustomAuditEventEntity_.eventDate));
            }
            if (criteria.getEventType() != null) {
                specification = specification.and(buildStringSpecification(criteria.getEventType(), CustomAuditEventEntity_.eventType));
            }
        }
        return specification;
    }
}
