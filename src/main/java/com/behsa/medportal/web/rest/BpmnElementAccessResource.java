package com.behsa.medportal.web.rest;

import com.behsa.medportal.service.BpmnElementAccessService;
import com.behsa.medportal.service.dto.BpmnElementAccessDTO;
import com.behsa.medportal.web.rest.errors.BadRequestAlertException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only API used by the BPMN editor to resolve element visibility for a product. */
@RestController
@RequestMapping("/api/bpmn-element-access")
public class BpmnElementAccessResource {

    private static final String ENTITY_NAME = "flow";

    private final BpmnElementAccessService bpmnElementAccessService;

    public BpmnElementAccessResource(BpmnElementAccessService bpmnElementAccessService) {
        this.bpmnElementAccessService = bpmnElementAccessService;
    }

    @GetMapping("/products/{productId}")
    @Secured(ENTITY_NAME)
    public ResponseEntity<BpmnElementAccessDTO> getProductAccess(@PathVariable Long productId) {
        try {
            return ResponseEntity.ok(bpmnElementAccessService.getAccess(productId));
        } catch (IllegalArgumentException exception) {
            throw new BadRequestAlertException(exception.getMessage(), ENTITY_NAME, "productnotfound");
        }
    }
}
