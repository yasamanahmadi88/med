package com.behsa.medportal.web.rest;

import com.behsa.medportal.service.PortalOwnerService;
import com.behsa.medportal.service.dto.PortalOwnerDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing the server-resolved portal Owner.
 *
 * The client cannot choose or override the Owner.
 */
@RestController
@RequestMapping("/api/portal-owner")
public class PortalOwnerResource {

    private final PortalOwnerService portalOwnerService;

    public PortalOwnerResource(PortalOwnerService portalOwnerService) {
        this.portalOwnerService = portalOwnerService;
    }

    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    public PortalOwnerDTO getCurrentOwner() {
        return PortalOwnerDTO.from(portalOwnerService.getCurrentOwner());
    }
}