package com.behsa.medportal.repository;

import com.behsa.medportal.domain.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link Authority} entity.
 */
public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    Optional<Authority> findByName(String string);
}
