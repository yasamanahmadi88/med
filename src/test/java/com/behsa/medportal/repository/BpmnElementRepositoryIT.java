package com.behsa.medportal.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.config.BpmnOwnerSchema;
import com.behsa.medportal.domain.BpmnElementEntity;
import com.behsa.medportal.domain.BpmnElementGroupEntity;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class BpmnElementRepositoryIT {

    private static final long MEDIATION_OWNER_ID = 1000L;
    private static final long DISABLED_TEST_OWNER_ID = 2000L;
    private static final long UNASSIGNED_OWNER_ID = 3000L;

    @Autowired
    private BpmnElementRepository bpmnElementRepository;

    @Autowired
    private BpmnElementGroupRepository bpmnElementGroupRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    void createMappingTables() {
        BpmnOwnerSchema.ensure(jdbcTemplate);
    }

    @Test
    void shouldReturnExactlySixFileProcessingElementsForMediationOwner() {
        BpmnElementGroupEntity fileProcessingGroup = saveGroup(
            "FILE_PROCESSING",
            "File Processing",
            1
        );

        BpmnElementEntity fileReceiver = saveElement(
            "FILE_RECEIVER",
            "FileReceiver:FileReceiver",
            "FileReceiver",
            "fileReceiver",
            "create.file-receiver",
            "File Receiver",
            1,
            10
        );

        BpmnElementEntity fileTransmitter = saveElement(
            "FILE_TRANSMITTER",
            "FileTransmitter:FileTransmitter",
            "FileTransmitter",
            "fileTransmitter",
            "create.file-transmitter",
            "File Transmitter",
            1,
            20
        );

        BpmnElementEntity merger = saveElement(
            "MERGER",
            "Merger:Merger",
            "Merger",
            "merger",
            "create.merger",
            "Merger",
            1,
            30
        );

        BpmnElementEntity fragmenter = saveElement(
            "FRAGMENTER",
            "Fragmenter:Fragmenter",
            "Fragmenter",
            "fragmenter",
            "create.fragmenter",
            "Fragmenter",
            1,
            40
        );

        BpmnElementEntity cdrParser = saveElement(
            "CDR_PARSER",
            "CdrParser:CdrParser",
            "CdrParser",
            "cdrParser",
            "create.cdr-parser",
            "CDR Parser",
            1,
            50
        );

        BpmnElementEntity csvTransformer = saveElement(
            "CSV_TRANSFORMER",
            "CsvTransformer:CsvTransformer",
            "CsvTransformer",
            "csvTransformer",
            "create.csv-transformer",
            "CSV Transformer",
            1,
            60
        );

        /*
         * Exists in the global element catalog but is intentionally
         * NOT assigned to the MEDIATION Owner's FILE_PROCESSING group.
         */
        saveElement(
            "KAFKA_RECEIVER",
            "KafkaReceiver:KafkaReceiver",
            "KafkaReceiver",
            "kafkaReceiver",
            "create.kafka-receiver",
            "Kafka Receiver",
            1,
            70
        );

        assignGroupToOwner(
            MEDIATION_OWNER_ID,
            fileProcessingGroup,
            1
        );

        assignElementToGroup(fileProcessingGroup, fileReceiver);
        assignElementToGroup(fileProcessingGroup, fileTransmitter);
        assignElementToGroup(fileProcessingGroup, merger);
        assignElementToGroup(fileProcessingGroup, fragmenter);
        assignElementToGroup(fileProcessingGroup, cdrParser);
        assignElementToGroup(fileProcessingGroup, csvTransformer);

        List<BpmnElementGroupEntity> groups =
            bpmnElementGroupRepository.findEnabledByOwnerId(
                MEDIATION_OWNER_ID
            );

        List<BpmnElementEntity> elements =
            bpmnElementRepository.findEnabledByOwnerId(
                MEDIATION_OWNER_ID
            );

        assertThat(groups)
            .extracting(BpmnElementGroupEntity::getGroupCode)
            .containsExactly("FILE_PROCESSING");

        assertThat(elements)
            .extracting(BpmnElementEntity::getElementCode)
            .containsExactly(
                "FILE_RECEIVER",
                "FILE_TRANSMITTER",
                "MERGER",
                "FRAGMENTER",
                "CDR_PARSER",
                "CSV_TRANSFORMER"
            );

        assertThat(elements)
            .extracting(BpmnElementEntity::getElementCode)
            .doesNotContain("KAFKA_RECEIVER");
    }

    @Test
    void shouldIgnoreDisabledMappingsGroupsAndElements() {
        BpmnElementGroupEntity enabledGroup = saveGroup(
            "ENABLED_GROUP",
            "Enabled Group",
            1
        );

        BpmnElementGroupEntity disabledGroup = saveGroup(
            "DISABLED_GROUP",
            "Disabled Group",
            0
        );

        BpmnElementGroupEntity disabledMappingGroup = saveGroup(
            "DISABLED_MAPPING_GROUP",
            "Disabled Mapping Group",
            1
        );

        BpmnElementEntity enabledElement = saveElement(
            "ENABLED_ELEMENT",
            "Test:EnabledElement",
            "urn:test:bpmn",
            "enabledElement",
            "create.enabled-element",
            "Enabled Element",
            1,
            10
        );

        BpmnElementEntity disabledElement = saveElement(
            "DISABLED_ELEMENT",
            "Test:DisabledElement",
            "urn:test:bpmn",
            "disabledElement",
            "create.disabled-element",
            "Disabled Element",
            0,
            20
        );

        BpmnElementEntity elementInDisabledGroup = saveElement(
            "DISABLED_GROUP_ELEMENT",
            "Test:DisabledGroupElement",
            "urn:test:bpmn",
            "disabledGroupElement",
            "create.disabled-group-element",
            "Disabled Group Element",
            1,
            30
        );

        BpmnElementEntity elementInDisabledMapping = saveElement(
            "DISABLED_MAPPING_ELEMENT",
            "Test:DisabledMappingElement",
            "urn:test:bpmn",
            "disabledMappingElement",
            "create.disabled-mapping-element",
            "Disabled Mapping Element",
            1,
            40
        );

        assignGroupToOwner(
            DISABLED_TEST_OWNER_ID,
            enabledGroup,
            1
        );

        assignGroupToOwner(
            DISABLED_TEST_OWNER_ID,
            disabledGroup,
            1
        );

        /*
         * The group itself is enabled, but this Owner-to-Group
         * assignment is disabled.
         */
        assignGroupToOwner(
            DISABLED_TEST_OWNER_ID,
            disabledMappingGroup,
            0
        );

        assignElementToGroup(enabledGroup, enabledElement);
        assignElementToGroup(enabledGroup, disabledElement);
        assignElementToGroup(
            disabledGroup,
            elementInDisabledGroup
        );
        assignElementToGroup(
            disabledMappingGroup,
            elementInDisabledMapping
        );

        List<BpmnElementGroupEntity> groups =
            bpmnElementGroupRepository.findEnabledByOwnerId(
                DISABLED_TEST_OWNER_ID
            );

        List<BpmnElementEntity> elements =
            bpmnElementRepository.findEnabledByOwnerId(
                DISABLED_TEST_OWNER_ID
            );

        assertThat(groups)
            .extracting(BpmnElementGroupEntity::getGroupCode)
            .containsExactly("ENABLED_GROUP");

        assertThat(elements)
            .extracting(BpmnElementEntity::getElementCode)
            .containsExactly("ENABLED_ELEMENT");
    }

    @Test
    void shouldReturnEmptyAccessWhenOwnerHasNoGroupMapping() {
        BpmnElementGroupEntity group = saveGroup(
            "UNASSIGNED_GROUP",
            "Unassigned Group",
            1
        );

        BpmnElementEntity element = saveElement(
            "UNASSIGNED_ELEMENT",
            "Test:UnassignedElement",
            "urn:test:bpmn",
            "unassignedElement",
            "create.unassigned-element",
            "Unassigned Element",
            1,
            10
        );

        /*
         * Catalog data exists but this Owner has no assignment.
         * Access must therefore fail closed to an empty result.
         */
        assignElementToGroup(group, element);

        List<BpmnElementGroupEntity> groups =
            bpmnElementGroupRepository.findEnabledByOwnerId(
                UNASSIGNED_OWNER_ID
            );

        List<BpmnElementEntity> elements =
            bpmnElementRepository.findEnabledByOwnerId(
                UNASSIGNED_OWNER_ID
            );

        assertThat(groups).isEmpty();
        assertThat(elements).isEmpty();
    }

    private BpmnElementGroupEntity saveGroup(
        String groupCode,
        String groupName,
        int enabled
    ) {
        BpmnElementGroupEntity group =
            new BpmnElementGroupEntity();

        group.setGroupCode(groupCode);
        group.setGroupName(groupName);
        group.setEnabled(enabled);

        return bpmnElementGroupRepository.saveAndFlush(group);
    }

    private BpmnElementEntity saveElement(
        String elementCode,
        String bpmnType,
        String namespaceUri,
        String localName,
        String paletteAction,
        String displayName,
        int enabled,
        int sortOrder
    ) {
        BpmnElementEntity element =
            new BpmnElementEntity();

        element.setElementCode(elementCode);
        element.setBpmnType(bpmnType);
        element.setNamespaceUri(namespaceUri);
        element.setLocalName(localName);
        element.setPaletteAction(paletteAction);
        element.setDisplayName(displayName);
        element.setEnabled(enabled);
        element.setSortOrder(sortOrder);

        return bpmnElementRepository.saveAndFlush(element);
    }

    private void assignGroupToOwner(
        long ownerId,
        BpmnElementGroupEntity group,
        int enabled
    ) {
        jdbcTemplate.update(
            """
            INSERT INTO TBL_OWNER_BPMN_GROUP (
                owner_key,
                group_key,
                enabled
            )
            VALUES (?, ?, ?)
            """,
            ownerId,
            group.getId(),
            enabled
        );
    }

    private void assignElementToGroup(
        BpmnElementGroupEntity group,
        BpmnElementEntity element
    ) {
        jdbcTemplate.update(
            """
            INSERT INTO TBL_BPMN_GROUP_ELEMENT (
                group_key,
                element_key
            )
            VALUES (?, ?)
            """,
            group.getId(),
            element.getId()
        );
    }
}