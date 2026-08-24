package com.behsa.medportal.service;

import com.behsa.medportal.service.dto.ResourceDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.behsa.medportal.domain.ResourceEntity}.
 */
public interface ResourceService {
    /**
     * Save a resource.
     *
     * @param resourceDTO the entity to save.
     * @return the persisted entity.
     */
    ResourceDTO save(ResourceDTO resourceDTO);

    /**
     * Updates a resource.
     *
     * @param resourceDTO the entity to update.
     * @return the persisted entity.
     */
    ResourceDTO update(ResourceDTO resourceDTO);

    /**
     * Partially updates a resource.
     *
     * @param resourceDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ResourceDTO> partialUpdate(ResourceDTO resourceDTO);

    /**
     * Get all the resources.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ResourceDTO> findAll(Pageable pageable);

    /**
     * Get the "id" resource.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ResourceDTO> findOne(Long id);

    /**
     * Delete the "id" resource.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
