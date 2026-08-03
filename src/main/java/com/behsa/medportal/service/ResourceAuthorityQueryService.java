package com.behsa.medportal.service;

import com.behsa.medportal.domain.*; // for static metamodels
import com.behsa.medportal.domain.ResourceAuthorityEntity;
import com.behsa.medportal.repository.MedAuthorityRepository;
import com.behsa.medportal.repository.ResourceAuthorityRepository;
import com.behsa.medportal.service.criteria.ResourceAuthorityCriteria;
import com.behsa.medportal.service.dto.ResourceAuthorityDTO;
import com.behsa.medportal.service.mapper.ResourceAuthorityMapper;

import java.util.ArrayList;
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
 * Service for executing complex queries for {@link ResourceAuthorityEntity} entities in the database.
 * The main input is a {@link ResourceAuthorityCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link ResourceAuthorityDTO} or a {@link Page} of {@link ResourceAuthorityDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ResourceAuthorityQueryService extends QueryService<ResourceAuthorityEntity> {

    private final Logger log = LoggerFactory.getLogger(ResourceAuthorityQueryService.class);

    private final ResourceAuthorityRepository resourceAuthorityRepository;

    private final ResourceAuthorityMapper resourceAuthorityMapper;

    private final MedAuthorityRepository medAuthorityRepository;

    private final MedAuthorityService medAuthorityService;

    private final ResourceService resourceService;

    public ResourceAuthorityQueryService(
        ResourceAuthorityRepository resourceAuthorityRepository,
        ResourceAuthorityMapper resourceAuthorityMapper,
        MedAuthorityRepository medAuthorityRepository,
        MedAuthorityService medAuthorityService,
        ResourceService resourceService
    ) {
        this.resourceAuthorityRepository = resourceAuthorityRepository;
        this.resourceAuthorityMapper = resourceAuthorityMapper;
        this.medAuthorityRepository = medAuthorityRepository;
        this.medAuthorityService = medAuthorityService;
        this.resourceService = resourceService;
    }

    /**
     * Return a {@link List} of {@link ResourceAuthorityDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<ResourceAuthorityDTO> findByCriteria(ResourceAuthorityCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<ResourceAuthorityEntity> specification = createSpecification(criteria);
        return resourceAuthorityMapper.toDto(resourceAuthorityRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link ResourceAuthorityDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ResourceAuthorityDTO> findByCriteria(ResourceAuthorityCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<ResourceAuthorityEntity> specification = createSpecification(criteria);
        return resourceAuthorityRepository.findAll(specification, page).map(resourceAuthorityMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ResourceAuthorityDTO> findByAuthorities(List<String> authorities, Pageable page) {
        log.debug("find by criteria : {}, page: {}", authorities, page);
        if (authorities == null || authorities.isEmpty()) {
            return Page.empty(page);
        }
        List<Long> authIds = new ArrayList<>();
        List<MedAuthorityEntity> authorityList = medAuthorityRepository.findByNameIn(authorities);
        authorityList.forEach(authority -> authIds.add(authority.getId()));
        if (authIds.isEmpty()) {
            return Page.empty(page);
        }
        Page<ResourceAuthorityDTO> temp = resourceAuthorityRepository
            .findByMedAuthority_IdIn(authIds, page)
            .map(resourceAuthorityMapper::toDto);
        temp.forEach(
            x -> {
                if (x.getMedAuthority() != null && x.getMedAuthority().getId() != null) {
                    medAuthorityService.findOne(x.getMedAuthority().getId()).ifPresent(x::setMedAuthority);
                }
                if (x.getResource() != null && x.getResource().getId() != null) {
                    resourceService.findOne(x.getResource().getId()).ifPresent(x::setResource);
                }
            }
        );
        return temp;
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ResourceAuthorityCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<ResourceAuthorityEntity> specification = createSpecification(criteria);
        return resourceAuthorityRepository.count(specification);
    }

    /**
     * Function to convert {@link ResourceAuthorityCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<ResourceAuthorityEntity> createSpecification(ResourceAuthorityCriteria criteria) {
        Specification<ResourceAuthorityEntity> specification = (root, query, criteriaBuilder) -> null;
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), ResourceAuthorityEntity_.id));
            }
            if (criteria.getVerb() != null) {
                specification = specification.and(buildSpecification(criteria.getVerb(), ResourceAuthorityEntity_.verb));
            }
            if (criteria.getMedAuthorityId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getMedAuthorityId(),
                            root -> root.join(ResourceAuthorityEntity_.medAuthority, JoinType.LEFT).get(MedAuthorityEntity_.id)
                        )
                    );
            }
            if (criteria.getResourceId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getResourceId(),
                            root -> root.join(ResourceAuthorityEntity_.resource, JoinType.LEFT).get(ResourceEntity_.id)
                        )
                    );
            }
        }
        return specification;
    }
}
