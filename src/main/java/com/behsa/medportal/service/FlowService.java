package com.behsa.medportal.service;

import com.behsa.medportal.domain.FlowEntity;
import com.behsa.medportal.service.dto.FlowDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link FlowEntity}.
 */
public interface FlowService {
    /**
     * Save a flow.
     *
     * @param flowDTO the entity to save.
     * @return the persisted entity.
     */
    FlowDTO save(FlowDTO flowDTO);

    /**
     * Updates a flow.
     *
     * @param flowDTO the entity to update.
     * @return the persisted entity.
     */
    FlowDTO update(FlowDTO flowDTO);

    /**
     * Partially updates a flow.
     *
     * @param flowDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<FlowDTO> partialUpdate(FlowDTO flowDTO);

    /**
     * Get all the flows.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<FlowDTO> findAll(Pageable pageable);

    /**
     * Get the "id" flow.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<FlowDTO> findOne(Long id);

    /**
     * Delete the "id" flow.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
