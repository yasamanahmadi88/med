package com.behsa.medportal.service;

import com.behsa.medportal.domain.*; // for static metamodels
import com.behsa.medportal.domain.MedAuthorityEntity;
import com.behsa.medportal.repository.MedAuthorityRepository;
import com.behsa.medportal.service.criteria.MedAuthorityCriteria;
import com.behsa.medportal.service.dto.MedAuthorityDTO;
import com.behsa.medportal.service.mapper.MedAuthorityMapper;
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
 * Service for executing complex queries for {@link MedAuthorityEntity} entities in the database.
 * The main input is a {@link MedAuthorityCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link MedAuthorityDTO} or a {@link Page} of {@link MedAuthorityDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class MedAuthorityQueryService extends QueryService<MedAuthorityEntity> {

    private final Logger log = LoggerFactory.getLogger(MedAuthorityQueryService.class);

    private final MedAuthorityRepository medAuthorityRepository;

    private final MedAuthorityMapper medAuthorityMapper;

    public MedAuthorityQueryService(MedAuthorityRepository medAuthorityRepository, MedAuthorityMapper medAuthorityMapper) {
        this.medAuthorityRepository = medAuthorityRepository;
        this.medAuthorityMapper = medAuthorityMapper;
    }

    /**
     * Return a {@link List} of {@link MedAuthorityDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<MedAuthorityDTO> findByCriteria(MedAuthorityCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<MedAuthorityEntity> specification = createSpecification(criteria);
        return medAuthorityMapper.toDto(medAuthorityRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link MedAuthorityDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<MedAuthorityDTO> findByCriteria(MedAuthorityCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<MedAuthorityEntity> specification = createSpecification(criteria);
        return medAuthorityRepository.findAll(specification, page).map(medAuthorityMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(MedAuthorityCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<MedAuthorityEntity> specification = createSpecification(criteria);
        return medAuthorityRepository.count(specification);
    }

    /**
     * Function to convert {@link MedAuthorityCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<MedAuthorityEntity> createSpecification(MedAuthorityCriteria criteria) {
        Specification<MedAuthorityEntity> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), MedAuthorityEntity_.id));
            }
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), MedAuthorityEntity_.name));
            }
            if (criteria.getDisplayName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDisplayName(), MedAuthorityEntity_.displayName));
            }
            if (criteria.getParentId() != null) {
                specification = specification.and(buildSpecification(criteria.getParentId(),
                    root -> root.join(MedAuthorityEntity_.parent, JoinType.LEFT).get(MedAuthorityEntity_.id)));
            }
            if (criteria.getResourceAuthoritiesId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getResourceAuthoritiesId(),
                            root -> root.join(MedAuthorityEntity_.resourceAuthorities, JoinType.LEFT).get(ResourceAuthorityEntity_.id)
                        )
                    );
            }
        }
        return specification;
    }
}
