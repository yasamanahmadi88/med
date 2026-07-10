package com.behsa.medportal.service;

import com.behsa.medportal.domain.*;
import com.behsa.medportal.repository.VersionRepository;
import com.behsa.medportal.service.criteria.VersionCriteria;
import com.behsa.medportal.service.dto.VersionDTO;
import com.behsa.medportal.service.mapper.VersionMapper;
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
 * Service for executing complex queries for {@link VersionEntity} entities in the database.
 * The main input is a {@link VersionCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link VersionDTO} or a {@link Page} of {@link VersionDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class VersionQueryService extends QueryService<VersionEntity> {

    private final Logger log = LoggerFactory.getLogger(VersionQueryService.class);

    private final VersionRepository versionRepository;

    private final VersionMapper versionMapper;

    public VersionQueryService(VersionRepository versionRepository, VersionMapper versionMapper) {
        this.versionRepository = versionRepository;
        this.versionMapper = versionMapper;
    }

    /**
     * Return a {@link List} of {@link VersionDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<VersionDTO> findByCriteria(VersionCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<VersionEntity> specification = createSpecification(criteria);
        return versionMapper.toDto(versionRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link VersionDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<VersionDTO> findByCriteria(VersionCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<VersionEntity> specification = createSpecification(criteria);
        return versionRepository.findAll(specification, page).map(versionMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(VersionCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<VersionEntity> specification = createSpecification(criteria);
        return versionRepository.count(specification);
    }

    /**
     * Function to convert {@link VersionCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<VersionEntity> createSpecification(VersionCriteria criteria) {
        Specification<VersionEntity> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), VersionEntity_.id));
            }
            if (criteria.getTableName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getTableName(), VersionEntity_.tableName));
            }
            if (criteria.getModuleName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getModuleName(), VersionEntity_.moduleName));
            }
            if (criteria.getTableVersion() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getTableVersion(), VersionEntity_.tableVersion));
            }
        }
        return specification;
    }
}
