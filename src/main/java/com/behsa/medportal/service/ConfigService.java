package com.behsa.medportal.service;

import com.behsa.medportal.domain.ConfigEntity;
import com.behsa.medportal.service.dto.ConfigDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link ConfigEntity}.
 */
public interface ConfigService {
    /**
     * Save a config.
     *
     * @param configDTO the entity to save.
     * @return the persisted entity.
     */
    ConfigDTO save(ConfigDTO configDTO);

    /**
     * Updates a config.
     *
     * @param configDTO the entity to update.
     * @return the persisted entity.
     */
    ConfigDTO update(ConfigDTO configDTO);

    /**
     * Partially updates a config.
     *
     * @param configDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ConfigDTO> partialUpdate(ConfigDTO configDTO);

    /**
     * Get all the configs.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ConfigDTO> findAll(Pageable pageable);

    /**
     * Get the "id" config.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ConfigDTO> findOne(Long id);

    /**
     * Delete the "id" config.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
