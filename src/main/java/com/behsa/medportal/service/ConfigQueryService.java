package com.behsa.medportal.service;

import com.behsa.medportal.domain.*;
import com.behsa.medportal.repository.ConfigRepository;
import com.behsa.medportal.service.criteria.ConfigCriteria;
import com.behsa.medportal.service.dto.ConfigDTO;
import com.behsa.medportal.service.mapper.ConfigMapper;
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
 * Service for executing complex queries for {@link ConfigEntity} entities in the database.
 * The main input is a {@link ConfigCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link ConfigDTO} or a {@link Page} of {@link ConfigDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ConfigQueryService extends QueryService<ConfigEntity> {

    private final Logger log = LoggerFactory.getLogger(ConfigQueryService.class);

    private final ConfigRepository configRepository;

    private final ConfigMapper configMapper;

    public ConfigQueryService(ConfigRepository configRepository, ConfigMapper configMapper) {
        this.configRepository = configRepository;
        this.configMapper = configMapper;
    }

    /**
     * Return a {@link List} of {@link ConfigDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<ConfigDTO> findByCriteria(ConfigCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<ConfigEntity> specification = createSpecification(criteria);
        return configMapper.toDto(configRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link ConfigDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ConfigDTO> findByCriteria(ConfigCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<ConfigEntity> specification = createSpecification(criteria);
        return configRepository.findAll(specification, page).map(configMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ConfigCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<ConfigEntity> specification = createSpecification(criteria);
        return configRepository.count(specification);
    }

    /**
     * Function to convert {@link ConfigCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<ConfigEntity> createSpecification(ConfigCriteria criteria) {
        Specification<ConfigEntity> specification = (root, query, criteriaBuilder) -> null;
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), ConfigEntity_.id));
            }
            if (criteria.getProperty() != null) {
                specification = specification.and(buildStringSpecification(criteria.getProperty(), ConfigEntity_.property));
            }
            if (criteria.getpValue() != null) {
                specification = specification.and(buildStringSpecification(criteria.getpValue(), ConfigEntity_.pValue));
            }
            if (criteria.getCommentDesc() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCommentDesc(), ConfigEntity_.commentDesc));
            }
            if (criteria.getModuleId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getModuleId(),
                            root -> root.join(ConfigEntity_.module, JoinType.LEFT).get(ModuleEntity_.id)
                        )
                    );
            }
        }
        return specification;
    }
}
