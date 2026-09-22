package com.behsa.medportal.service;

import com.behsa.medportal.domain.BpmnElementEntity;
import com.behsa.medportal.domain.BpmnElementGroupEntity;
import com.behsa.medportal.domain.PortalOwnerEntity;
import com.behsa.medportal.repository.BpmnElementGroupRepository;
import com.behsa.medportal.repository.BpmnElementRepository;
import com.behsa.medportal.service.BpmnXmlElementScanner.XmlElementKey;
import com.behsa.medportal.service.dto.BpmnElementAccessDTO;
import java.util.HashSet;
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

    /**
     * Element codes withheld from every Owner: they stay mapped in the catalog so diagrams that
     * already contain them keep working, but none may be placed into a diagram that did not have
     * them. Shared by the palette snapshot and by save-time validation so the two cannot drift —
     * hiding an entry in the browser is not authorization.
     */
    private static final Set<String> RESTRICTED_ELEMENT_CODES = Set.of("CDR_PARSER", "CSV_TRANSFORMER");

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
                .filter(element -> !RESTRICTED_ELEMENT_CODES.contains(element.getElementCode()))
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
     * Returns every access-controlled element in the BPMN XML that the active portal Owner may not
     * place. Equivalent to {@link #findDisallowedElements(String, String)} with no prior document,
     * so nothing counts as carried over — which is what a create needs.
     */
    public Set<XmlElementKey> findDisallowedElements(String xml) {
        return findDisallowedElements(xml, null);
    }

    /**
     * Returns every access-controlled element in the submitted BPMN XML that the active portal
     * Owner may not place, treating a restricted type as permissible where {@code persistedXml} —
     * the diagram currently stored for this flow — already carried it.
     *
     * <p>That carry-over is what keeps a restricted type (see {@link #RESTRICTED_ELEMENT_CODES})
     * viewable, editable and saveable in the flows that already use it, without letting anyone
     * introduce a new one. It is granted per type, and only when <em>every</em> instance of that
     * type in the submitted diagram sits at an id that held the same type before: keeping one CDR
     * parser and adding a second is refused, and so is an instance carrying no id, which cannot be
     * matched against anything and therefore counts as new.
     *
     * <p>Owner resolution intentionally happens before scanning so missing Owner configuration
     * always fails closed, including for an otherwise-empty diagram.
     *
     * @param persistedXml the stored diagram, or {@code null} when there is none to carry from.
     */
    public Set<XmlElementKey> findDisallowedElements(String xml, String persistedXml) {
        PortalOwnerEntity owner = portalOwnerService.getCurrentOwner();

        Set<XmlElementKey> used = xmlElementScanner.scan(xml);

        if (used.isEmpty()) {
            return Set.of();
        }

        Set<XmlElementKey> allowed = new HashSet<>();
        Set<XmlElementKey> restricted = new HashSet<>();
        for (BpmnElementEntity element : elementRepository.findEnabledByOwnerId(owner.getId())) {
            XmlElementKey key = new XmlElementKey(element.getNamespaceUri(), element.getLocalName());
            if (RESTRICTED_ELEMENT_CODES.contains(element.getElementCode())) {
                restricted.add(key);
            } else {
                allowed.add(key);
            }
        }

        Set<XmlElementKey> denied = used
            .stream()
            .filter(element -> !allowed.contains(element))
            .collect(Collectors.toCollection(LinkedHashSet::new));

        if (denied.isEmpty() || persistedXml == null || persistedXml.isBlank()) {
            return denied;
        }

        Map<String, XmlElementKey> submittedIds = xmlElementScanner.scanInstances(xml);
        Map<String, XmlElementKey> persistedIds = xmlElementScanner.scanInstances(persistedXml);
        Map<XmlElementKey, Long> submittedCounts = xmlElementScanner.countInstances(xml);

        denied.removeIf(key -> restricted.contains(key) && entirelyCarriedOver(key, submittedIds, persistedIds, submittedCounts));

        return denied;
    }

    /**
     * True when every instance of {@code key} in the submitted diagram sits at an id that already
     * held that same type. Matching ids alone would miss an instance carrying none — invisible to
     * an id-keyed scan — so the matched ids have to account for the type's full instance count.
     */
    private boolean entirelyCarriedOver(
        XmlElementKey key,
        Map<String, XmlElementKey> submittedIds,
        Map<String, XmlElementKey> persistedIds,
        Map<XmlElementKey, Long> submittedCounts
    ) {
        long carriedOver = submittedIds
            .entrySet()
            .stream()
            .filter(entry -> key.equals(entry.getValue()) && key.equals(persistedIds.get(entry.getKey())))
            .count();

        return carriedOver > 0 && carriedOver == submittedCounts.getOrDefault(key, 0L);
    }
}