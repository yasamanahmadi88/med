package com.behsa.medportal.service;

import com.behsa.medportal.service.dto.MedAuthorityDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.behsa.medportal.domain.MedAuthorityEntity}.
 */
public interface MedAuthorityService {
    /**
     * Save a medAuthority.
     *
     * @param medAuthorityDTO the entity to save.
     * @return the persisted entity.
     */
    MedAuthorityDTO save(MedAuthorityDTO medAuthorityDTO);

    /**
     * Updates a medAuthority.
     *
     * @param medAuthorityDTO the entity to update.
     * @return the persisted entity.
     */
    MedAuthorityDTO update(MedAuthorityDTO medAuthorityDTO);

    /**
     * Partially updates a medAuthority.
     *
     * @param medAuthorityDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<MedAuthorityDTO> partialUpdate(MedAuthorityDTO medAuthorityDTO);

    /**
     * Get all the medAuthorities.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<MedAuthorityDTO> findAll(Pageable pageable);

    /**
     * Get the "id" medAuthority.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<MedAuthorityDTO> findOne(Long id);

    /**
     * Delete the "id" medAuthority.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
