package com.behsa.medportal.service;

import com.behsa.medportal.domain.BpmnElementEntity;
import com.behsa.medportal.domain.BpmnElementGroupEntity;
import com.behsa.medportal.domain.PortalOwnerEntity;
import com.behsa.medportal.repository.BpmnElementGroupRepository;
import com.behsa.medportal.repository.BpmnElementRepository;
import com.behsa.medportal.service.BpmnXmlElementScanner.XmlElementKey;
import com.behsa.medportal.service.dto.BpmnElementAccessDTO;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resolves active Portal Owner -> Groups -> BPMN Elements.
 *
 * Neither Flow Product nor authenticated user role participates in BPMN capability resolution.
 */
@Service
@Transactional(readOnly = true)
public class BpmnElementAccessService {

    private static final Set<XmlElementKey> LEGACY_VIEW_EDIT_ONLY_TYPES = Set.of(
        new XmlElementKey("CdrParser", "cdrParser"),
        new XmlElementKey("CsvTransformer", "csvTransformer")
    );

    private final PortalOwnerService portalOwnerService;
    private final BpmnElementRepository elementRepository;
    private final BpmnElementGroupRepository groupRepository;
    private final BpmnXmlElementScanner xmlElementScanner;

    public BpmnElementAccessService(
        PortalOwnerService portalOwnerService,
        BpmnElementRepository elementRepository,
        BpmnElementGroupRepository groupRepository,
        BpmnXmlElementScanner xmlElementScanner
    ) {
        this.portalOwnerService = portalOwnerService;
        this.elementRepository = elementRepository;
        this.groupRepository = groupRepository;
        this.xmlElementScanner = xmlElementScanner;
    }

    public BpmnElementAccessDTO getCurrentAccess() {
        PortalOwnerEntity owner = portalOwnerService.getCurrentOwner();

        List<BpmnElementGroupEntity> groups =
            groupRepository.findEnabledByOwnerId(owner.getId());

        List<BpmnElementEntity> elements =
            elementRepository.findEnabledByOwnerId(owner.getId());

        BpmnElementAccessDTO dto = new BpmnElementAccessDTO();
        dto.setOwnerCode(owner.getOwnerCode());
        dto.setOwnerDisplayName(owner.getDisplayName());

        dto.setGroups(
            groups
                .stream()
                .map(group ->
                    new BpmnElementAccessDTO.GroupDTO(
                        group.getId(),
                        group.getGroupCode(),
                        group.getGroupName()
                    )
                )
                .toList()
        );

        dto.setElements(
            elements
                .stream()
                .map(element ->
                    new BpmnElementAccessDTO.ElementDTO(
                        element.getId(),
                        element.getElementCode(),
                        element.getBpmnType(),
                        element.getNamespaceUri(),
                        element.getLocalName(),
                        element.getPaletteAction(),
                        element.getDisplayName(),
                        element.getSortOrder()
                    )
                )
                .toList()
        );

        return dto;
    }

    /**
     * Update validation: existing CDR/CSV ids keep view/edit support, while a new id or a type
     * change to either retired module remains forbidden. Deleting a legacy instance is allowed.
     */
    public Set<XmlElementKey> findDisallowedElements(String xml, String persistedXml) {
        Set<XmlElementKey> denied = new LinkedHashSet<>(findDisallowedElements(xml));
        Map<String, XmlElementKey> previous = xmlElementScanner.scanInstances(persistedXml);
        Map<String, XmlElementKey> current = xmlElementScanner.scanInstances(xml);

        denied.removeAll(LEGACY_VIEW_EDIT_ONLY_TYPES);
        current.entrySet()
            .stream()
            .filter(entry -> LEGACY_VIEW_EDIT_ONLY_TYPES.contains(entry.getValue()))
            .filter(entry -> !entry.getValue().equals(previous.get(entry.getKey())))
            .map(Map.Entry::getValue)
            .forEach(denied::add);

        return denied;
    }

    /**
     * Returns every access-controlled element in the BPMN XML that is not
     * enabled for the active portal Owner.
     *
     * Owner resolution intentionally happens before scanning so missing Owner
     * configuration always fails closed, including for an otherwise-empty diagram.
     */
    public Set<XmlElementKey> findDisallowedElements(String xml) {
        PortalOwnerEntity owner = portalOwnerService.getCurrentOwner();

        Set<XmlElementKey> used = xmlElementScanner.scan(xml);

        if (used.isEmpty()) {
            return Set.of();
        }

        Set<XmlElementKey> allowed = elementRepository
            .findEnabledByOwnerId(owner.getId())
            .stream()
            .map(element ->
                new XmlElementKey(
                    element.getNamespaceUri(),
                    element.getLocalName()
                )
            )
            .collect(Collectors.toSet());

        return used
            .stream()
            // POST/create must reject CDR/CSV even if obsolete DB mappings still allow them.
            .filter(element -> LEGACY_VIEW_EDIT_ONLY_TYPES.contains(element) || !allowed.contains(element))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
