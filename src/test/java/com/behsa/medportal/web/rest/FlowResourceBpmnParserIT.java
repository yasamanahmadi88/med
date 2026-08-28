package com.behsa.medportal.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.domain.FlowEntity;
import com.behsa.medportal.repository.FlowRepository;
import com.behsa.medportal.service.dto.FlowDTO;
import com.behsa.medportal.service.mapper.FlowMapper;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
 * <p>The parser is a validation/dispatch step rather than the system of record (see
 * {@link FlowResource#sendToBpmnParser}), so these tests pin down two things: an accepted flow is still
 * persisted in the local database, and a rejected flow is not persisted at all.
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

    private static final String DEFAULT_FLOW = "AAAAAAAAAA";
    private static final String UPDATED_FLOW = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/flows";
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

    private FlowEntity flowEntity;

    @BeforeEach
    public void initTest() {
        flowEntity = FlowResourceIT.createEntity(em);
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
    void createFlowWithParserActiveStillPersistsLocally() throws Exception {
        givenParserAccepts();

        int databaseSizeBeforeCreate = flowRepository.findAll().size();

        FlowDTO flowDTO = flowMapper.toDto(flowEntity);
        restFlowMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(flowDTO)))
            .andExpect(status().isCreated());

        // The flow went to the parser ...
        verify(restTemplate).postForEntity(contains("/create"), any(HttpEntity.class), eq(Object.class));

        // ... and it is still stored locally.
        List<FlowEntity> flowList = flowRepository.findAll();
        assertThat(flowList).hasSize(databaseSizeBeforeCreate + 1);
        FlowEntity testFlow = flowList.get(flowList.size() - 1);
        assertThat(testFlow.getFlowName()).isEqualTo(DEFAULT_FLOW_NAME);
        assertThat(testFlow.getFlowDesc()).isEqualTo(DEFAULT_FLOW_DESC);
        assertThat(testFlow.getFlow()).isEqualTo(DEFAULT_FLOW);
    }

    @Test
    @Transactional
    void createFlowIsNotPersistedWhenParserRejects() throws Exception {
        givenParserRejects();

        int databaseSizeBeforeCreate = flowRepository.findAll().size();

        FlowDTO flowDTO = flowMapper.toDto(flowEntity);
        restFlowMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(flowDTO)))
            .andExpect(status().isBadRequest());

        // Nothing was stored: the parser verdict wins.
        List<FlowEntity> flowList = flowRepository.findAll();
        assertThat(flowList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void updateFlowWithParserActiveStillPersistsLocally() throws Exception {
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
            .andExpect(status().isOk());

        // The flow went to the parser ...
        verify(restTemplate).postForEntity(contains("/update"), any(HttpEntity.class), eq(Object.class));

        // ... and the local row was updated instead of going stale.
        List<FlowEntity> flowList = flowRepository.findAll();
        assertThat(flowList).hasSize(databaseSizeBeforeUpdate);
        FlowEntity testFlow = flowList.get(flowList.size() - 1);
        assertThat(testFlow.getFlowName()).isEqualTo(UPDATED_FLOW_NAME);
        assertThat(testFlow.getFlowDesc()).isEqualTo(UPDATED_FLOW_DESC);
        assertThat(testFlow.getFlow()).isEqualTo(UPDATED_FLOW);
    }

    @Test
    @Transactional
    void updateFlowIsNotPersistedWhenParserRejects() throws Exception {
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
            .andExpect(status().isBadRequest());

        // The stored row is untouched.
        List<FlowEntity> flowList = flowRepository.findAll();
        assertThat(flowList).hasSize(databaseSizeBeforeUpdate);
        FlowEntity testFlow = flowRepository.findById(flowEntity.getId()).orElseThrow();
        assertThat(testFlow.getFlowName()).isEqualTo(DEFAULT_FLOW_NAME);
        assertThat(testFlow.getFlowDesc()).isEqualTo(DEFAULT_FLOW_DESC);
        assertThat(testFlow.getFlow()).isEqualTo(DEFAULT_FLOW);
    }
}
