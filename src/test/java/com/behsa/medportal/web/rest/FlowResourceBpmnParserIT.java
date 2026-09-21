package com.behsa.medportal.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.domain.FlowEntity;
import com.behsa.medportal.repository.FlowRepository;
import com.behsa.medportal.service.dto.FlowDTO;
import com.behsa.medportal.service.mapper.FlowMapper;
import jakarta.persistence.EntityManager;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * Integration tests for the {@link FlowResource} create/update paths with the external mediation BPMN
 * parser switched on ({@code mediation.bpmn.parser.active=true}; it is {@code false} in every shipped
 * configuration, which is why {@link FlowResourceIT} covers the parser-inactive behaviour).
 *
 * <p>With the parser on, create and update hand the flow over to it and return its response without
 * touching the local database (see {@link FlowResource#sendToBpmnParser}). That is deliberate — the
 * mediation team owns the flow once the parser is enabled — and surprising enough that these tests
 * pin it down, so nobody "fixes" it back into a double-write by accident.
 *
 * <p>The parser call goes through the shared {@link RestTemplate} bean, replaced here by a
 * {@link MockitoBean} so that no HTTP request leaves the test.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
@TestPropertySource(
    properties = { "mediation.bpmn.parser.active=true", "mediation.bpmn.parser.url=http://bpmn-parser.invalid" }
)
class FlowResourceBpmnParserIT {

    private static final String DEFAULT_FLOW_NAME = "AAAAAA";
    private static final String UPDATED_FLOW_NAME = "BBBBBB";

    private static final String DEFAULT_FLOW_DESC = "AAAAAAAAAA";
    private static final String UPDATED_FLOW_DESC = "BBBBBBBBBB";

    private static final String DEFAULT_FLOW = """
        <?xml version="1.0" encoding="UTF-8"?>
        <bpmn:definitions
            xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
            id="Definitions_Default"
            targetNamespace="http://bpmn.io/schema/bpmn">
          <bpmn:process id="Process_Default" isExecutable="true" />
        </bpmn:definitions>
        """;
    private static final String UPDATED_FLOW = """
        <?xml version="1.0" encoding="UTF-8"?>
        <bpmn:definitions
            xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
            id="Definitions_Updated"
            targetNamespace="http://bpmn.io/schema/bpmn">
          <bpmn:process id="Process_Updated" isExecutable="true" />
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
    private static final String ENTITY_API_URL = "/api/flows";
    private static final long OWNER_ID = 93001L;
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private FlowRepository flowRepository;

    @Autowired
    private FlowMapper flowMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restFlowMockMvc;

    @MockitoBean
    private RestTemplate restTemplate;



    @Autowired
    private JdbcTemplate jdbcTemplate;
    private FlowEntity flowEntity;

    @BeforeEach
    public void initTest() {
        ensureOwnerInfrastructure();
        configureActiveOwner();
        flowEntity = FlowResourceIT.createEntity(em);
    }

    private void ensureOwnerInfrastructure() {
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

    private void configureActiveOwner() {
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
            "PARSER_TEST",
            "Parser Test",
            "Parser Test Portal",
            "BPMN parser integration-test Owner",
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
    private void givenParserAccepts() {
        ResponseEntity<Object> accepted = ResponseEntity.ok(Map.of("status", "parsed"));
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Object.class))).thenReturn(accepted);
    }

    private void givenParserRejects() {
        ResponseEntity<Object> rejected = ResponseEntity.badRequest().body(Map.of("error", "invalid bpmn"));
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Object.class))).thenReturn(rejected);
    }
    @Test
    @Transactional
    void disallowedBpmnElementIsRejectedBeforeExternalParserCall() throws Exception {


        int databaseSizeBeforeCreate = flowRepository.findAll().size();

        flowEntity.setFlow(DISALLOWED_KAFKA_XML);
        FlowDTO flowDTO = flowMapper.toDto(flowEntity);

        restFlowMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(flowDTO))
            )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value("error.bpmnelementnotallowed")
            )
            .andExpect(jsonPath("$.params").value("flow"));

        verifyNoInteractions(restTemplate);

        assertThat(flowRepository.findAll())
            .hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void createFlowWithParserActiveDelegatesInsteadOfPersisting() throws Exception {
        givenParserAccepts();

        int databaseSizeBeforeCreate = flowRepository.findAll().size();

        FlowDTO flowDTO = flowMapper.toDto(flowEntity);
        restFlowMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(flowDTO)))
            // The parser's own response is returned verbatim, so this is its 200 rather than the 201
            // the parser-inactive path produces.
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("parsed"));

        verify(restTemplate).postForEntity(contains("/create"), any(HttpEntity.class), eq(Object.class));

        // Nothing is written locally: the parser owns the flow while it is enabled.
        assertThat(flowRepository.findAll()).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void createFlowReturnsTheParserRejection() throws Exception {
        givenParserRejects();

        int databaseSizeBeforeCreate = flowRepository.findAll().size();

        FlowDTO flowDTO = flowMapper.toDto(flowEntity);
        restFlowMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(flowDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("invalid bpmn"));

        assertThat(flowRepository.findAll()).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void updateFlowWithParserActiveDelegatesAndLeavesTheRowUntouched() throws Exception {
        // Initialize the database
        flowRepository.saveAndFlush(flowEntity);
        givenParserAccepts();

        int databaseSizeBeforeUpdate = flowRepository.findAll().size();

        // Update the flow
        FlowEntity updatedFlowEntity = flowRepository.findById(flowEntity.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedFlowEntity are not directly saved in db
        em.detach(updatedFlowEntity);
        updatedFlowEntity.flowName(UPDATED_FLOW_NAME).flowDesc(UPDATED_FLOW_DESC).flow(UPDATED_FLOW);
        FlowDTO flowDTO = flowMapper.toDto(updatedFlowEntity);

        restFlowMockMvc
            .perform(
                put(ENTITY_API_URL_ID, flowDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(flowDTO))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("parsed"));

        verify(restTemplate).postForEntity(contains("/update"), any(HttpEntity.class), eq(Object.class));

        // The stored row keeps its original values — the update went to the parser, not to the database,
        // which is why the list and detail screens can disagree with what was last submitted.
        assertThat(flowRepository.findAll()).hasSize(databaseSizeBeforeUpdate);
        FlowEntity testFlow = flowRepository.findById(flowEntity.getId()).orElseThrow();
        assertThat(testFlow.getFlowName()).isEqualTo(DEFAULT_FLOW_NAME);
        assertThat(testFlow.getFlowDesc()).isEqualTo(DEFAULT_FLOW_DESC);
        assertThat(testFlow.getFlow()).isEqualTo(DEFAULT_FLOW);
    }

    @Test
    @Transactional
    void updateFlowReturnsTheParserRejection() throws Exception {
        // Initialize the database
        flowRepository.saveAndFlush(flowEntity);
        givenParserRejects();

        int databaseSizeBeforeUpdate = flowRepository.findAll().size();

        FlowEntity updatedFlowEntity = flowRepository.findById(flowEntity.getId()).orElseThrow();
        em.detach(updatedFlowEntity);
        updatedFlowEntity.flowName(UPDATED_FLOW_NAME).flowDesc(UPDATED_FLOW_DESC).flow(UPDATED_FLOW);
        FlowDTO flowDTO = flowMapper.toDto(updatedFlowEntity);

        restFlowMockMvc
            .perform(
                put(ENTITY_API_URL_ID, flowDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(flowDTO))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("invalid bpmn"));

        assertThat(flowRepository.findAll()).hasSize(databaseSizeBeforeUpdate);
        FlowEntity testFlow = flowRepository.findById(flowEntity.getId()).orElseThrow();
        assertThat(testFlow.getFlowName()).isEqualTo(DEFAULT_FLOW_NAME);
        assertThat(testFlow.getFlowDesc()).isEqualTo(DEFAULT_FLOW_DESC);
        assertThat(testFlow.getFlow()).isEqualTo(DEFAULT_FLOW);
    }
}
