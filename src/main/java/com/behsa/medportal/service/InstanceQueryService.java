package com.behsa.medportal.service;

import com.behsa.medportal.domain.*;
import com.behsa.medportal.repository.InstanceRepository;
import com.behsa.medportal.service.criteria.InstanceCriteria;
import com.behsa.medportal.service.dto.InstanceDTO;
import com.behsa.medportal.service.mapper.InstanceMapper;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link InstanceEntity} entities in the database.
 * The main input is a {@link InstanceCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link InstanceDTO} or a {@link Page} of {@link InstanceDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class InstanceQueryService extends QueryService<InstanceEntity> {

    private final Logger log = LoggerFactory.getLogger(InstanceQueryService.class);

    private final InstanceRepository instanceRepository;

    private final InstanceMapper instanceMapper;

    public InstanceQueryService(InstanceRepository instanceRepository, InstanceMapper instanceMapper) {
        this.instanceRepository = instanceRepository;
        this.instanceMapper = instanceMapper;
    }

    /**
     * Return a {@link List} of {@link InstanceDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<InstanceDTO> findByCriteria(InstanceCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<InstanceEntity> specification = createSpecification(criteria);
        return instanceMapper.toDto(instanceRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link InstanceDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<InstanceDTO> findByCriteria(InstanceCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<InstanceEntity> specification = createSpecification(criteria);
        return instanceRepository.findAll(specification, page).map(instanceMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(InstanceCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<InstanceEntity> specification = createSpecification(criteria);
        return instanceRepository.count(specification);
    }

    /**
     * Function to convert {@link InstanceCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<InstanceEntity> createSpecification(InstanceCriteria criteria) {
        Specification<InstanceEntity> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), InstanceEntity_.id));
            }
            if (criteria.getModuleName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getModuleName(), InstanceEntity_.moduleName));
            }
            if (criteria.getPort() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPort(), InstanceEntity_.port));
            }
            if (criteria.getModuleStatus() != null) {
                specification = specification.and(buildStringSpecification(criteria.getModuleStatus(), InstanceEntity_.moduleStatus));
            }
            if (criteria.getLastUpdateDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getLastUpdateDate(), InstanceEntity_.lastUpdateDate));
            }
            if (criteria.getThreadPoolQueueSize() != null) {
                specification =
                    specification.and(buildRangeSpecification(criteria.getThreadPoolQueueSize(), InstanceEntity_.threadPoolQueueSize));
            }
            if (criteria.getModuleStartTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getModuleStartTime(), InstanceEntity_.moduleStartTime));
            }
            if (criteria.getTotalProcessedWork() != null) {
                specification =
                    specification.and(buildRangeSpecification(criteria.getTotalProcessedWork(), InstanceEntity_.totalProcessedWork));
            }
            if (criteria.getTotalProcessedTask() != null) {
                specification =
                    specification.and(buildRangeSpecification(criteria.getTotalProcessedTask(), InstanceEntity_.totalProcessedTask));
            }
            if (criteria.getProcessedStatistics() != null) {
                specification =
                    specification.and(buildStringSpecification(criteria.getProcessedStatistics(), InstanceEntity_.processedStatistics));
            }
            if (criteria.getHostName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getHostName(), InstanceEntity_.hostName));
            }
        }
        return specification;
    }
}
