package com.behsa.medportal.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.domain.BpmnElementEntity;
import com.behsa.medportal.domain.BpmnElementGroupEntity;
import com.behsa.medportal.domain.ProductEntity;
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

    @Autowired
    private BpmnElementRepository bpmnElementRepository;

    @Autowired
    private BpmnElementGroupRepository bpmnElementGroupRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    void createMappingTables() {
        jdbcTemplate.execute(
            """
            CREATE TABLE IF NOT EXISTS TBL_BPMN_GROUP_ELEMENT (
                group_key BIGINT NOT NULL,
                element_key BIGINT NOT NULL,
                PRIMARY KEY (group_key, element_key)
            )
            """
        );

        jdbcTemplate.execute(
            """
            CREATE TABLE IF NOT EXISTS TBL_PRODUCT_BPMN_GROUP (
                product_key BIGINT NOT NULL,
                group_key BIGINT NOT NULL,
                PRIMARY KEY (product_key, group_key)
            )
            """
        );
    }

    @Test
    void shouldReturnExactlySixFileProcessingElementsForMci() {
        ProductEntity product = saveProduct("MCI");

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

        // Exists in the catalog, but is intentionally NOT assigned to MCI.
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

        assignGroupToProduct(product, fileProcessingGroup);

        assignElementToGroup(fileProcessingGroup, fileReceiver);
        assignElementToGroup(fileProcessingGroup, fileTransmitter);
        assignElementToGroup(fileProcessingGroup, merger);
        assignElementToGroup(fileProcessingGroup, fragmenter);
        assignElementToGroup(fileProcessingGroup, cdrParser);
        assignElementToGroup(fileProcessingGroup, csvTransformer);

        List<BpmnElementGroupEntity> groups =
            bpmnElementGroupRepository.findEnabledByProductId(product.getId());

        List<BpmnElementEntity> elements =
            bpmnElementRepository.findEnabledByProductId(product.getId());

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
    void shouldIgnoreDisabledGroupsAndDisabledElements() {
        ProductEntity product = saveProduct("DIS");

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

        assignGroupToProduct(product, enabledGroup);
        assignGroupToProduct(product, disabledGroup);

        assignElementToGroup(enabledGroup, enabledElement);
        assignElementToGroup(enabledGroup, disabledElement);
        assignElementToGroup(disabledGroup, elementInDisabledGroup);

        List<BpmnElementGroupEntity> groups =
            bpmnElementGroupRepository.findEnabledByProductId(product.getId());

        List<BpmnElementEntity> elements =
            bpmnElementRepository.findEnabledByProductId(product.getId());

        assertThat(groups)
            .extracting(BpmnElementGroupEntity::getGroupCode)
            .containsExactly("ENABLED_GROUP");

        assertThat(elements)
            .extracting(BpmnElementEntity::getElementCode)
            .containsExactly("ENABLED_ELEMENT");
    }

    @Test
    void shouldReturnEmptyAccessWhenProductHasNoGroupMapping() {
        ProductEntity product = saveProduct("NMP");

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

        // Group and element exist, but product is deliberately not assigned to the group.
        assignElementToGroup(group, element);

        List<BpmnElementGroupEntity> groups =
            bpmnElementGroupRepository.findEnabledByProductId(product.getId());

        List<BpmnElementEntity> elements =
            bpmnElementRepository.findEnabledByProductId(product.getId());

        assertThat(groups).isEmpty();
        assertThat(elements).isEmpty();
    }

    private ProductEntity saveProduct(String productName) {
        ProductEntity product = new ProductEntity();
        product.setProductName(productName);
        product.setProductDesc("Repository integration test product " + productName);

        return productRepository.saveAndFlush(product);
    }

    private BpmnElementGroupEntity saveGroup(
        String groupCode,
        String groupName,
        int enabled
    ) {
        BpmnElementGroupEntity group = new BpmnElementGroupEntity();
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
        BpmnElementEntity element = new BpmnElementEntity();
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

    private void assignGroupToProduct(
        ProductEntity product,
        BpmnElementGroupEntity group
    ) {
        jdbcTemplate.update(
            """
            INSERT INTO TBL_PRODUCT_BPMN_GROUP (product_key, group_key)
            VALUES (?, ?)
            """,
            product.getId(),
            group.getId()
        );
    }

    private void assignElementToGroup(
        BpmnElementGroupEntity group,
        BpmnElementEntity element
    ) {
        jdbcTemplate.update(
            """
            INSERT INTO TBL_BPMN_GROUP_ELEMENT (group_key, element_key)
            VALUES (?, ?)
            """,
            group.getId(),
            element.getId()
        );
    }
}