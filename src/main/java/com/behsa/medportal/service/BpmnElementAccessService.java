package com.behsa.medportal.service;

import com.behsa.medportal.domain.BpmnElementEntity;
import com.behsa.medportal.domain.BpmnElementGroupEntity;
import com.behsa.medportal.domain.ProductEntity;
import com.behsa.medportal.repository.BpmnElementGroupRepository;
import com.behsa.medportal.repository.BpmnElementRepository;
import com.behsa.medportal.repository.ProductRepository;
import com.behsa.medportal.service.BpmnXmlElementScanner.XmlElementKey;
import com.behsa.medportal.service.dto.BpmnElementAccessDTO;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Resolves Product -> Groups -> BPMN Elements and validates BPMN XML against that configuration. */
@Service
@Transactional(readOnly = true)
public class BpmnElementAccessService {

    private final ProductRepository productRepository;
    private final BpmnElementRepository elementRepository;
    private final BpmnElementGroupRepository groupRepository;
    private final BpmnXmlElementScanner xmlElementScanner;

    public BpmnElementAccessService(
        ProductRepository productRepository,
        BpmnElementRepository elementRepository,
        BpmnElementGroupRepository groupRepository,
        BpmnXmlElementScanner xmlElementScanner
    ) {
        this.productRepository = productRepository;
        this.elementRepository = elementRepository;
        this.groupRepository = groupRepository;
        this.xmlElementScanner = xmlElementScanner;
    }

    public BpmnElementAccessDTO getAccess(Long productId) {
        ProductEntity product = productRepository
            .findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        List<BpmnElementGroupEntity> groups = groupRepository.findEnabledByProductId(productId);
        List<BpmnElementEntity> elements = elementRepository.findEnabledByProductId(productId);

        BpmnElementAccessDTO dto = new BpmnElementAccessDTO();
        dto.setProductId(product.getId());
        dto.setProductName(product.getProductName());
        dto.setGroups(
            groups.stream().map(group -> new BpmnElementAccessDTO.GroupDTO(group.getId(), group.getGroupCode(), group.getGroupName())).toList()
        );
        dto.setElements(
            elements
                .stream()
                .map(element -> new BpmnElementAccessDTO.ElementDTO(
                    element.getId(),
                    element.getElementCode(),
                    element.getBpmnType(),
                    element.getNamespaceUri(),
                    element.getLocalName(),
                    element.getPaletteAction(),
                    element.getDisplayName(),
                    element.getSortOrder()
                ))
                .toList()
        );
        return dto;
    }

    /** Returns every access-controlled element present in the XML that the product is not allowed to use. */
    public Set<XmlElementKey> findDisallowedElements(Long productId, String xml) {
        Set<XmlElementKey> used = xmlElementScanner.scan(xml);
        if (used.isEmpty()) {
            return Set.of();
        }

        Set<XmlElementKey> allowed = elementRepository
            .findEnabledByProductId(productId)
            .stream()
            .map(element -> new XmlElementKey(element.getNamespaceUri(), element.getLocalName()))
            .collect(Collectors.toSet());

        return used.stream().filter(element -> !allowed.contains(element)).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
