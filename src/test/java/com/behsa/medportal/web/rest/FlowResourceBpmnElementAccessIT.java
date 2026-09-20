package com.behsa.medportal.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.domain.BpmnElementEntity;
import com.behsa.medportal.domain.BpmnElementGroupEntity;
import com.behsa.medportal.domain.FlowEntity;
import com.behsa.medportal.domain.ProductEntity;
import com.behsa.medportal.repository.BpmnElementGroupRepository;
import com.behsa.medportal.repository.BpmnElementRepository;
import com.behsa.medportal.repository.FlowRepository;
import com.behsa.medportal.repository.ProductRepository;
import com.behsa.medportal.service.dto.FlowDTO;
import com.behsa.medportal.service.mapper.FlowMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class FlowResourceBpmnElementAccessIT {

    private static final String ENTITY_API_URL = "/api/flows";

    private static final long OWNER_ID = 92001L;

    private static final String ALLOWED_MERGER_XML = """
        <?xml version="1.0" encoding="UTF-8"?>
        <bpmn:definitions
            xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
            xmlns:Merger="Merger"
            id="Definitions_Allowed"
            targetNamespace="http://bpmn.io/schema/bpmn">
          <bpmn:process id="Process_Allowed" isExecutable="true">
            <Merger:merger id="Activity_Merger" />
          </bpmn:process>
        </bpmn:definitions>
        """;

    private static final String DISALLOWED_KAFKA_XML = """
        <?xml version="1.0" encoding="UTF-8"?>
        <bpmn:definitions
            xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
            xmlns:KafkaReceiver="KafkaReceiver"
            id="Definitions_Disallowed"
            targetNamespace="http://bpmn.io/schema/bpmn">
          <bpmn:process id="Process_Disallowed" isExecutable="true">
            <KafkaReceiver:kafkaReceiver id="Activity_Kafka" />
          </bpmn:process>
        </bpmn:definitions>
        """;

    private static final String MALFORMED_XML = """
        <?xml version="1.0" encoding="UTF-8"?>
        <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL">
          <bpmn:process id="Broken_Process">
        </bpmn:definitions>
        """;

    private static final String DOCTYPE_XML = """
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE bpmn:definitions [
          <!ENTITY xxe SYSTEM "file:///etc/passwd">
        ]>
        <bpmn:definitions
            xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
            id="Definitions_Unsafe"
            targetNamespace="http://bpmn.io/schema/bpmn">
          <bpmn:process id="Process_Unsafe" />
        </bpmn:definitions>
        """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FlowRepository flowRepository;

    @Autowired
    private FlowMapper flowMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BpmnElementRepository bpmnElementRepository;

    @Autowired
    private BpmnElementGroupRepository bpmnElementGroupRepository;

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
            CREATE TABLE IF NOT EXISTS TBL_OWNER_BPMN_GROUP (
                owner_key BIGINT NOT NULL,
                group_key BIGINT NOT NULL,
                enabled INTEGER DEFAULT 1 NOT NULL,
                PRIMARY KEY (owner_key, group_key)
            )
            """
        );

        jdbcTemplate.execute(
            """
            CREATE TABLE IF NOT EXISTS TBL_PORTAL_OWNER (
                owner_key BIGINT NOT NULL PRIMARY KEY,
                owner_code VARCHAR(50) NOT NULL,
                owner_name VARCHAR(100) NOT NULL,
                display_name VARCHAR(150) NOT NULL,
                description VARCHAR(500),
                enabled INTEGER DEFAULT 1 NOT NULL
            )
            """
        );

        jdbcTemplate.execute(
            """
            CREATE TABLE IF NOT EXISTS TBL_PORTAL_CONFIGURATION (
                config_key BIGINT NOT NULL PRIMARY KEY,
                active_owner_key BIGINT NOT NULL
            )
            """
        );
    }

    @BeforeEach
    void configureActiveOwner() {
        jdbcTemplate.update(
            "DELETE FROM TBL_PORTAL_CONFIGURATION WHERE config_key = ?",
            1L
        );

        jdbcTemplate.update(
            "DELETE FROM TBL_PORTAL_OWNER WHERE owner_key = ?",
            OWNER_ID
        );

        jdbcTemplate.update(
            """
            INSERT INTO TBL_PORTAL_OWNER (
                owner_key,
                owner_code,
                owner_name,
                display_name,
                description,
                enabled
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """,
            OWNER_ID,
            "MEDIATION",
            "Mediation",
            "Mediation Portal",
            "Flow BPMN Owner access integration test",
            1
        );

        jdbcTemplate.update(
            """
            INSERT INTO TBL_PORTAL_CONFIGURATION (
                config_key,
                active_owner_key
            )
            VALUES (?, ?)
            """,
            1L,
            OWNER_ID
        );
    }

    @Test
    void shouldCreateFlowWhenOwnerAllowsBpmnElement()
        throws Exception {

        ProductEntity product = saveProduct("MCI");
        configureAllowedMerger();

        int databaseSizeBeforeCreate =
            flowRepository.findAll().size();

        FlowDTO flowDTO = newFlowDto(
            product,
            "ALW001",
            ALLOWED_MERGER_XML
        );

        mockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        TestUtil.convertObjectToJsonBytes(flowDTO)
                    )
            )
            .andExpect(status().isCreated());

        assertThat(flowRepository.findAll())
            .hasSize(databaseSizeBeforeCreate + 1);
    }

    @Test
    void shouldRejectFlowWhenOwnerCannotUseBpmnElement()
        throws Exception {

        ProductEntity product = saveProduct("MCI");
        configureAllowedMerger();

        int databaseSizeBeforeCreate =
            flowRepository.findAll().size();

        FlowDTO flowDTO = newFlowDto(
            product,
            "DEN001",
            DISALLOWED_KAFKA_XML
        );

        mockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        TestUtil.convertObjectToJsonBytes(flowDTO)
                    )
            )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value("error.bpmnelementnotallowed")
            )
            .andExpect(jsonPath("$.params").value("flow"));

        assertThat(flowRepository.findAll())
            .hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void shouldRejectFlowWhenRequiredProductIsMissing()
        throws Exception {

        int databaseSizeBeforeCreate =
            flowRepository.findAll().size();

        FlowEntity flow = new FlowEntity();
        flow.setFlowName("NOP001");
        flow.setFlowDesc(
            "Product is intentionally missing domain data"
        );
        flow.setFlow(ALLOWED_MERGER_XML);

        FlowDTO flowDTO = flowMapper.toDto(flow);

        mockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        TestUtil.convertObjectToJsonBytes(flowDTO)
                    )
            )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value("error.productrequired")
            )
            .andExpect(jsonPath("$.params").value("flow"));

        assertThat(flowRepository.findAll())
            .hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void shouldRejectMalformedBpmnXml() throws Exception {
        ProductEntity product = saveProduct("MCI");

        int databaseSizeBeforeCreate =
            flowRepository.findAll().size();

        FlowDTO flowDTO = newFlowDto(
            product,
            "BAD001",
            MALFORMED_XML
        );

        mockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        TestUtil.convertObjectToJsonBytes(flowDTO)
                    )
            )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value("error.invalidbpmnxml")
            )
            .andExpect(jsonPath("$.params").value("flow"));

        assertThat(flowRepository.findAll())
            .hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void shouldRejectBpmnXmlContainingDoctype()
        throws Exception {

        ProductEntity product = saveProduct("MCI");

        int databaseSizeBeforeCreate =
            flowRepository.findAll().size();

        FlowDTO flowDTO = newFlowDto(
            product,
            "XXE001",
            DOCTYPE_XML
        );

        mockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        TestUtil.convertObjectToJsonBytes(flowDTO)
                    )
            )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value("error.invalidbpmnxml")
            )
            .andExpect(jsonPath("$.params").value("flow"));

        assertThat(flowRepository.findAll())
            .hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void shouldUpdateFlowWhenOwnerAllowsBpmnElement()
        throws Exception {

        ProductEntity product = saveProduct("MCI");
        configureAllowedMerger();

        FlowEntity existing = saveExistingFlow(
            product,
            "OLD001",
            ALLOWED_MERGER_XML
        );

        FlowDTO flowDTO = newFlowDto(
            product,
            "UPD001",
            ALLOWED_MERGER_XML
        );

        flowDTO.setId(existing.getId());

        mockMvc
            .perform(
                put(
                    ENTITY_API_URL + "/{id}",
                    existing.getId()
                )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        TestUtil.convertObjectToJsonBytes(flowDTO)
                    )
            )
            .andExpect(status().isOk());

        FlowEntity updated = flowRepository
            .findById(existing.getId())
            .orElseThrow();

        assertThat(updated.getFlowName())
            .isEqualTo("UPD001");

        assertThat(updated.getFlow())
            .isEqualTo(ALLOWED_MERGER_XML);

        assertThat(updated.getProduct().getId())
            .isEqualTo(product.getId());
    }

    @Test
    void shouldRejectUpdateWhenOwnerCannotUseBpmnElement()
        throws Exception {

        ProductEntity product = saveProduct("MCI");
        configureAllowedMerger();

        FlowEntity existing = saveExistingFlow(
            product,
            "OLD002",
            ALLOWED_MERGER_XML
        );

        FlowDTO flowDTO = newFlowDto(
            product,
            "DEN002",
            DISALLOWED_KAFKA_XML
        );

        flowDTO.setId(existing.getId());

        mockMvc
            .perform(
                put(
                    ENTITY_API_URL + "/{id}",
                    existing.getId()
                )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        TestUtil.convertObjectToJsonBytes(flowDTO)
                    )
            )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value("error.bpmnelementnotallowed")
            )
            .andExpect(jsonPath("$.params").value("flow"));

        FlowEntity unchanged = flowRepository
            .findById(existing.getId())
            .orElseThrow();

        assertThat(unchanged.getFlowName())
            .isEqualTo("OLD002");

        assertThat(unchanged.getFlow())
            .isEqualTo(ALLOWED_MERGER_XML);
    }

    @Test
    void shouldRejectUpdateWhenRequiredProductIsMissing()
        throws Exception {

        ProductEntity product = saveProduct("MCI");
        configureAllowedMerger();

        FlowEntity existing = saveExistingFlow(
            product,
            "OLD003",
            ALLOWED_MERGER_XML
        );

        FlowDTO flowDTO = newFlowDto(
            product,
            "NOP002",
            ALLOWED_MERGER_XML
        );

        flowDTO.setId(existing.getId());
        flowDTO.setProduct(null);

        mockMvc
            .perform(
                put(
                    ENTITY_API_URL + "/{id}",
                    existing.getId()
                )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        TestUtil.convertObjectToJsonBytes(flowDTO)
                    )
            )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value("error.productrequired")
            )
            .andExpect(jsonPath("$.params").value("flow"));

        FlowEntity unchanged = flowRepository
            .findById(existing.getId())
            .orElseThrow();

        assertThat(unchanged.getFlowName())
            .isEqualTo("OLD003");

        assertThat(unchanged.getFlow())
            .isEqualTo(ALLOWED_MERGER_XML);
    }

    @Test
    void shouldRejectPatchWhenOnlyXmlBecomesDisallowed()
        throws Exception {

        ProductEntity product = saveProduct("MCI");
        configureAllowedMerger();

        FlowEntity existing = saveExistingFlow(
            product,
            "PAT001",
            ALLOWED_MERGER_XML
        );

        FlowDTO patchDTO = new FlowDTO();
        patchDTO.setId(existing.getId());
        patchDTO.setFlow(DISALLOWED_KAFKA_XML);

        mockMvc
            .perform(
                patch(
                    ENTITY_API_URL + "/{id}",
                    existing.getId()
                )
                    .contentType(
                        "application/merge-patch+json"
                    )
                    .content(
                        TestUtil.convertObjectToJsonBytes(patchDTO)
                    )
            )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value("error.bpmnelementnotallowed")
            )
            .andExpect(jsonPath("$.params").value("flow"));

        FlowEntity unchanged = flowRepository
            .findById(existing.getId())
            .orElseThrow();

        assertThat(unchanged.getFlowName())
            .isEqualTo("PAT001");

        assertThat(unchanged.getFlow())
            .isEqualTo(ALLOWED_MERGER_XML);

        assertThat(unchanged.getProduct().getId())
            .isEqualTo(product.getId());
    }

    @Test
    void shouldAllowProductChangeWhenOwnerAllowsExistingXml()
        throws Exception {

        ProductEntity originalProduct =
            saveProduct("MCI");

        ProductEntity alternateProduct =
            saveProduct("ALT");

        configureAllowedMerger();

        FlowEntity existing = saveExistingFlow(
            originalProduct,
            "PAT002",
            ALLOWED_MERGER_XML
        );

        FlowDTO patchDTO = newFlowDto(
            alternateProduct,
            "TMP001",
            null
        );

        patchDTO.setId(existing.getId());
        patchDTO.setFlowName(null);
        patchDTO.setFlowDesc(null);

        mockMvc
            .perform(
                patch(
                    ENTITY_API_URL + "/{id}",
                    existing.getId()
                )
                    .contentType(
                        "application/merge-patch+json"
                    )
                    .content(
                        TestUtil.convertObjectToJsonBytes(patchDTO)
                    )
            )
            .andExpect(status().isOk());

        FlowEntity updated = flowRepository
            .findById(existing.getId())
            .orElseThrow();

        /*
         * Product changes as normal Flow domain data.
         * The same Owner capability continues to authorize the XML.
         */
        assertThat(updated.getProduct().getId())
            .isEqualTo(alternateProduct.getId());

        assertThat(updated.getFlow())
            .isEqualTo(ALLOWED_MERGER_XML);
    }

    @Test
    void shouldPatchUnrelatedFieldUsingCurrentOwnerAccess()
        throws Exception {

        ProductEntity product = saveProduct("MCI");
        configureAllowedMerger();

        FlowEntity existing = saveExistingFlow(
            product,
            "PAT003",
            ALLOWED_MERGER_XML
        );

        FlowDTO patchDTO = new FlowDTO();
        patchDTO.setId(existing.getId());
        patchDTO.setFlowDesc(
            "Updated description only"
        );

        mockMvc
            .perform(
                patch(
                    ENTITY_API_URL + "/{id}",
                    existing.getId()
                )
                    .contentType(
                        "application/merge-patch+json"
                    )
                    .content(
                        TestUtil.convertObjectToJsonBytes(patchDTO)
                    )
            )
            .andExpect(status().isOk());

        FlowEntity updated = flowRepository
            .findById(existing.getId())
            .orElseThrow();

        assertThat(updated.getFlowDesc())
            .isEqualTo("Updated description only");

        assertThat(updated.getFlowName())
            .isEqualTo("PAT003");

        assertThat(updated.getFlow())
            .isEqualTo(ALLOWED_MERGER_XML);

        assertThat(updated.getProduct().getId())
            .isEqualTo(product.getId());
    }

    private FlowEntity saveExistingFlow(
        ProductEntity product,
        String flowName,
        String xml
    ) {
        FlowEntity flow = new FlowEntity();
        flow.setFlowName(flowName);
        flow.setFlowDesc(
            "Existing BPMN Owner access test flow"
        );
        flow.setFlow(xml);
        flow.setProduct(product);

        return flowRepository.saveAndFlush(flow);
    }

    private FlowDTO newFlowDto(
        ProductEntity product,
        String flowName,
        String xml
    ) {
        FlowEntity flow = new FlowEntity();
        flow.setFlowName(flowName);
        flow.setFlowDesc(
            "BPMN Owner access integration test"
        );
        flow.setFlow(xml);
        flow.setProduct(product);

        return flowMapper.toDto(flow);
    }

    private ProductEntity saveProduct(
        String productName
    ) {
        ProductEntity product =
            new ProductEntity();

        product.setProductName(productName);
        product.setProductDesc(
            "Flow domain product " + productName
        );

        return productRepository.saveAndFlush(product);
    }

    private void configureAllowedMerger() {
        BpmnElementGroupEntity group =
            new BpmnElementGroupEntity();

        group.setGroupCode("FILE_PROCESSING");
        group.setGroupName("File Processing");
        group.setEnabled(1);

        group =
            bpmnElementGroupRepository.saveAndFlush(group);

        BpmnElementEntity merger =
            new BpmnElementEntity();

        merger.setElementCode("MERGER");
        merger.setBpmnType("Merger:Merger");
        merger.setNamespaceUri("Merger");
        merger.setLocalName("merger");
        merger.setPaletteAction("create.merger");
        merger.setDisplayName("Merger");
        merger.setEnabled(1);
        merger.setSortOrder(10);

        merger =
            bpmnElementRepository.saveAndFlush(merger);

        jdbcTemplate.update(
            """
            INSERT INTO TBL_OWNER_BPMN_GROUP (
                owner_key,
                group_key,
                enabled
            )
            VALUES (?, ?, ?)
            """,
            OWNER_ID,
            group.getId(),
            1
        );

        jdbcTemplate.update(
            """
            INSERT INTO TBL_BPMN_GROUP_ELEMENT (
                group_key,
                element_key
            )
            VALUES (?, ?)
            """,
            group.getId(),
            merger.getId()
        );
    }
}