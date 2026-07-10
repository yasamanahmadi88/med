package com.behsa.medportal.service;

import com.behsa.medportal.domain.VersionEntity;
import com.behsa.medportal.service.dto.VersionDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link VersionEntity}.
 */
public interface VersionService {
    /**
     * Save a version.
     *
     * @param versionDTO the entity to save.
     * @return the persisted entity.
     */
    VersionDTO save(VersionDTO versionDTO);

    /**
     * Updates a version.
     *
     * @param versionDTO the entity to update.
     * @return the persisted entity.
     */
    VersionDTO update(VersionDTO versionDTO);

    /**
     * Partially updates a version.
     *
     * @param versionDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<VersionDTO> partialUpdate(VersionDTO versionDTO);

    /**
     * Get all the versions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<VersionDTO> findAll(Pageable pageable);

    /**
     * Get the "id" version.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<VersionDTO> findOne(Long id);

    /**
     * Delete the "id" version.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
