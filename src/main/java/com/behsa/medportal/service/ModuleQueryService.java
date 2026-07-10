package com.behsa.medportal.service;

import com.behsa.medportal.domain.*; // for static metamodels
import com.behsa.medportal.domain.ModuleEntity;
import com.behsa.medportal.repository.ModuleRepository;
import com.behsa.medportal.service.criteria.ModuleCriteria;
import com.behsa.medportal.service.dto.ModuleDTO;
import com.behsa.medportal.service.mapper.ModuleMapper;
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
 * Service for executing complex queries for {@link ModuleEntity} entities in the database.
 * The main input is a {@link ModuleCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link ModuleDTO} or a {@link Page} of {@link ModuleDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ModuleQueryService extends QueryService<ModuleEntity> {

    private final Logger log = LoggerFactory.getLogger(ModuleQueryService.class);

    private final ModuleRepository moduleRepository;

    private final ModuleMapper moduleMapper;

    public ModuleQueryService(ModuleRepository moduleRepository, ModuleMapper moduleMapper) {
        this.moduleRepository = moduleRepository;
        this.moduleMapper = moduleMapper;
    }

    /**
     * Return a {@link List} of {@link ModuleDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<ModuleDTO> findByCriteria(ModuleCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<ModuleEntity> specification = createSpecification(criteria);
        return moduleMapper.toDto(moduleRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link ModuleDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ModuleDTO> findByCriteria(ModuleCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<ModuleEntity> specification = createSpecification(criteria);
        return moduleRepository.findAll(specification, page).map(moduleMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ModuleCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<ModuleEntity> specification = createSpecification(criteria);
        return moduleRepository.count(specification);
    }

    /**
     * Function to convert {@link ModuleCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<ModuleEntity> createSpecification(ModuleCriteria criteria) {
        Specification<ModuleEntity> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), ModuleEntity_.id));
            }
            if (criteria.getModuleName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getModuleName(), ModuleEntity_.moduleName));
            }
            if (criteria.getDefaultPort() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDefaultPort(), ModuleEntity_.defaultPort));
            }
            if (criteria.getRedisKeyPrefix() != null) {
                specification = specification.and(buildStringSpecification(criteria.getRedisKeyPrefix(), ModuleEntity_.redisKeyPrefix));
            }
            if (criteria.getStatus() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getStatus(), ModuleEntity_.status));
            }
            if (criteria.getLoggingMode() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLoggingMode(), ModuleEntity_.loggingMode));
            }
            if (criteria.getLoggingFilter() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLoggingFilter(), ModuleEntity_.loggingFilter));
            }
            if (criteria.getDnsName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDnsName(), ModuleEntity_.dnsName));
            }
            if (criteria.getConfigsId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getConfigsId(),
                            root -> root.join(ModuleEntity_.configs, JoinType.LEFT).get(ConfigEntity_.id)
                        )
                    );
            }
        }
        return specification;
    }
}
