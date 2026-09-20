package com.behsa.medportal.service;

import com.behsa.medportal.service.dto.CustomAuditEventDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.behsa.medportal.domain.CustomAuditEventEntity}.
 */
public interface CustomAuditEventService {
    /**
     * Save a customAuditEvent.
     *
     * @param customAuditEventDTO the entity to save.
     * @return the persisted entity.
     */
    CustomAuditEventDTO save(CustomAuditEventDTO customAuditEventDTO);

    /**
     * Updates a customAuditEvent.
     *
     * @param customAuditEventDTO the entity to update.
     * @return the persisted entity.
     */
    CustomAuditEventDTO update(CustomAuditEventDTO customAuditEventDTO);

    /**
     * Partially updates a customAuditEvent.
     *
     * @param customAuditEventDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<CustomAuditEventDTO> partialUpdate(CustomAuditEventDTO customAuditEventDTO);

    /**
     * Get all the customAuditEvents.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CustomAuditEventDTO> findAll(Pageable pageable);

    /**
     * Get the "id" customAuditEvent.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CustomAuditEventDTO> findOne(Long id);

    /**
     * Delete the "id" customAuditEvent.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
