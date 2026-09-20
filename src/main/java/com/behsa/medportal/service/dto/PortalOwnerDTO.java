package com.behsa.medportal.service.dto;

import com.behsa.medportal.domain.PortalOwnerEntity;
import java.io.Serializable;

/**
 * Read-only public representation of the active portal Owner.
 *
 * Internal database/configuration details are deliberately not exposed.
 */
public final class PortalOwnerDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String code;
    private final String displayName;

    public PortalOwnerDTO(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static PortalOwnerDTO from(PortalOwnerEntity owner) {
        return new PortalOwnerDTO(owner.getOwnerCode(), owner.getDisplayName());
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }
}