package com.behsa.medportal.web.rest;

import com.behsa.medportal.service.BpmnElementAccessService;
import com.behsa.medportal.service.dto.BpmnElementAccessDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only BPMN capability API for the server-resolved active portal Owner.
 *
 * No Product or Owner selector is accepted from the browser.
 */
@RestController
@RequestMapping("/api/bpmn-element-access")
public class BpmnElementAccessResource {

    private static final String ENTITY_NAME = "flow";

    private final BpmnElementAccessService bpmnElementAccessService;

    public BpmnElementAccessResource(
        BpmnElementAccessService bpmnElementAccessService
    ) {
        this.bpmnElementAccessService = bpmnElementAccessService;
    }

    @GetMapping("/current")
    @Secured(ENTITY_NAME)
    public ResponseEntity<BpmnElementAccessDTO> getCurrentAccess() {
        return ResponseEntity.ok(
            bpmnElementAccessService.getCurrentAccess()
        );
    }
}