package com.behsa.medportal.service;

import com.behsa.medportal.domain.InstanceEntity;
import com.behsa.medportal.service.dto.InstanceDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link InstanceEntity}.
 */
public interface InstanceService {
    /**
     * Save a instance.
     *
     * @param instanceDTO the entity to save.
     * @return the persisted entity.
     */
    InstanceDTO save(InstanceDTO instanceDTO);

    /**
     * Updates a instance.
     *
     * @param instanceDTO the entity to update.
     * @return the persisted entity.
     */
    InstanceDTO update(InstanceDTO instanceDTO);

    /**
     * Partially updates a instance.
     *
     * @param instanceDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<InstanceDTO> partialUpdate(InstanceDTO instanceDTO);

    /**
     * Get all the instances.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<InstanceDTO> findAll(Pageable pageable);

    /**
     * Get the "id" instance.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<InstanceDTO> findOne(Long id);

    /**
     * Delete the "id" instance.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
