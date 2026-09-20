package com.behsa.medportal.service;

import com.behsa.medportal.domain.PortalOwnerEntity;
import com.behsa.medportal.repository.PortalOwnerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PortalOwnerService {

    private final PortalOwnerRepository portalOwnerRepository;

    public PortalOwnerService(PortalOwnerRepository portalOwnerRepository) {
        this.portalOwnerRepository = portalOwnerRepository;
    }

    /**
     * Resolves the Owner configured for this portal installation.
     *
     * Security rule:
     * Owner never comes from the authenticated user's role, Flow Product,
     * query parameters, request payload, cookies or browser storage.
     *
     * There is deliberately no Java-side fallback Owner.
     */
    public PortalOwnerEntity getCurrentOwner() {
        return portalOwnerRepository
            .findActiveOwner()
            .orElseThrow(() ->
                new IllegalStateException(
                    "No enabled active portal owner is configured"
                )
            );
    }
}