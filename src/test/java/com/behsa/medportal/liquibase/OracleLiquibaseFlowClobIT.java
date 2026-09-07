package com.behsa.medportal.liquibase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.domain.FlowEntity;
import com.behsa.medportal.domain.ProductEntity;
import com.behsa.medportal.repository.FlowRepository;
import com.behsa.medportal.service.dto.FlowDTO;
import com.behsa.medportal.service.mapper.FlowMapper;
import com.behsa.medportal.web.rest.CustomIconFlowFixture;
import com.behsa.medportal.web.rest.TestUtil;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * What {@code TBL_FLOWS.FLOW} does on Oracle when the schema is the production one.
 *
 * <p>This class exists because no other suite can answer that question. {@code FlowResourceIT} runs
 * against schemas Hibernate generates — H2 in the default profile, Oracle under
 * {@code -Poracle-testcontainers}, both with {@code ddl-auto} on. Only
 * {@code -Poracle-liquibase-testcontainers} runs Liquibase with {@code ddl-auto=none}, which is the
 * arrangement production uses and the only one where this column is the CLOB Liquibase declares
 * ({@code ${clobType}} in {@code 20260711_002_domain_schema.xml}).
 *
 * <p>Held here: that the production column really is a CLOB, that a diagram carrying a full
 * custom-icon library survives it byte for byte including its Persian process name, and that
 * {@code flow.contains} still searches the whole of it while {@code flow.equals} is refused before
 * it can reach Oracle.
 *
 * <p>{@code ROLE_ADMIN} rather than the plain {@code @WithMockUser} the entity ITs use: this profile
 * does not run {@code test-data.sql}, so the per-resource grants those tests rely on are not there.
 * Authorization is not what this class is about.
 */
@AutoConfigureMockMvc
@IntegrationTest
@WithMockUser(roles = "ADMIN")
class OracleLiquibaseFlowClobIT {

    private static final String ENTITY_API_URL = "/api/flows";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final String FLOW = CustomIconFlowFixture.flowWithIconLibrary(
        CustomIconFlowFixture.CREATE_PROCESS_ID,
        CustomIconFlowFixture.CREATE_TASK_ID
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FlowRepository flowRepository;

    @Autowired
    private FlowMapper flowMapper;

    @Autowired
    private EntityManager em;

    private ProductEntity product;

    @BeforeEach
    void createProduct() {
        product = new ProductEntity().productName("MED").productDesc("Oracle CLOB integration test");
    }

    @Test
    void flowColumnIsAClobInTheProductionSchema() {
        assertThat(columnType("FLOW")).isEqualTo("CLOB");
        // The neighbouring column, which Liquibase declares varchar(255), read the same way: without
        // it a query that returned the same answer for every column would satisfy the line above.
        assertThat(columnType("FLOW_NAME")).isEqualTo("VARCHAR2");
    }

    private String columnType(String column) {
        return jdbcTemplate.queryForObject(
            "SELECT data_type FROM user_tab_columns WHERE table_name = 'TBL_FLOWS' AND column_name = ?",
            String.class,
            column
        );
    }

    /**
     * The persistence context is cleared before the assertion, so the value compared is one Oracle
     * handed back rather than the instance the POST left in the session. Without that the test would
     * pass against a column that had truncated it.
     */
    @Test
    @Transactional
    void aDiagramWithACustomIconLibraryRoundTripsThroughTheProductionColumn() throws Exception {
        CustomIconFlowFixture.assertWithinEditorCaps();
        Long createdId = createFlow(FLOW);

        em.flush();
        em.clear();

        FlowEntity stored = flowRepository.findById(createdId).orElseThrow();
        assertThat(stored.getFlow()).hasSize(FLOW.length());
        assertThat(stored.getFlow()).isEqualTo(FLOW);
        // Separate assertion: an AL32UTF8 database is what makes this work, and a single-byte one
        // would still satisfy the length check on an otherwise ASCII document.
        assertThat(stored.getFlow()).contains(CustomIconFlowFixture.PERSIAN_PROCESS_NAME);

        mockMvc
            .perform(get(ENTITY_API_URL_ID, createdId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flow").value(FLOW));
    }

    /**
     * The needle is the placed task's id, which first appears after the whole ~192 KB icon library,
     * so Oracle has to search the body of the CLOB and not just its head.
     */
    @Test
    @Transactional
    void flowContainsSearchesTheWholeClob() throws Exception {
        Long createdId = createFlow(FLOW);
        em.flush();
        em.clear();

        mockMvc
            .perform(get(ENTITY_API_URL + "?flow.contains=" + CustomIconFlowFixture.CREATE_TASK_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[0].id").value(createdId.intValue()))
            .andExpect(jsonPath("$.[0].flow").value(FLOW));

        mockMvc
            .perform(get(ENTITY_API_URL + "/count?flow.contains=" + CustomIconFlowFixture.UPDATE_TASK_ID))
            .andExpect(status().isOk())
            .andExpect(content().string("0"));
    }

    /**
     * Oracle answers {@code =} and {@code IN} against a LOB with
     * {@code ORA-22848: cannot use CLOB type as comparison key}, whatever the value's length. The
     * request is refused before it gets there.
     */
    @Test
    @Transactional
    void flowEqualsAndFlowInAreRefusedBeforeTheyReachOracle() throws Exception {
        createFlow(FLOW);
        em.flush();
        em.clear();

        mockMvc
            .perform(get(ENTITY_API_URL + "?flow.equals=" + CustomIconFlowFixture.CREATE_TASK_ID))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorKey").value("flownotcomparable"));

        mockMvc
            .perform(get(ENTITY_API_URL + "?flow.in=" + CustomIconFlowFixture.CREATE_TASK_ID))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorKey").value("flownotcomparable"));
    }

    private Long createFlow(String flow) throws Exception {
        em.persist(product);
        em.flush();

        FlowEntity flowEntity = new FlowEntity().flowName("CLOBIT").flowDesc("custom icon library round trip").flow(flow);
        flowEntity.setProduct(product);

        FlowDTO flowDTO = flowMapper.toDto(flowEntity);
        MvcResult created = mockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(flowDTO)))
            .andExpect(status().isCreated())
            .andReturn();

        return ((Number) JsonPath.read(created.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.id")).longValue();
    }
}
