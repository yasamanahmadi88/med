package com.behsa.medportal.liquibase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.repository.AuthorityRepository;
import com.behsa.medportal.repository.MedAuthorityRepository;
import com.behsa.medportal.repository.ResourceRepository;
import com.behsa.medportal.repository.UserRepository;
import com.behsa.medportal.security.captcha.CaptchaValidationService;
import com.behsa.medportal.security.jwt.JWTFilter;
import com.behsa.medportal.web.rest.TestUtil;
import com.behsa.medportal.web.rest.vm.LoginVM;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import javax.sql.DataSource;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifies production-shaped Oracle schema: Liquibase ON, Hibernate ddl-auto=none.
 */
@AutoConfigureMockMvc
@IntegrationTest
class OracleLiquibaseSchemaIT {

    private static final String TEST_CAPTCHA_ID = "oracle-liquibase-captcha-id";
    private static final String TEST_CAPTCHA_TOKEN = "oracle-liquibase-captcha-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private MedAuthorityRepository medAuthorityRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @MockitoBean
    private CaptchaValidationService captchaValidationService;

    @Test
    void liquibaseCreatedNumericAuthoritySchemaAndDomainTables() throws Exception {
        assertThat(tableExists("JHI_AUTHORITY")).isTrue();
        assertThat(columnExists("JHI_AUTHORITY", "ID")).isTrue();
        assertThat(columnExists("JHI_AUTHORITY", "NAME")).isTrue();
        assertThat(columnExists("JHI_AUTHORITY", "DISPLAY_NAME")).isTrue();
        assertThat(columnExists("JHI_USER_AUTHORITY", "AUTHORITY_ID")).isTrue();
        assertThat(columnExists("JHI_USER", "PARTY_ID")).isTrue();
        assertThat(tableExists("JHI_RESOURCE")).isTrue();
        assertThat(tableExists("JHI_RESOURCE_AUTHORITY")).isTrue();
        assertThat(tableExists("TBL_MODULES")).isTrue();
        assertThat(tableExists("TBL_PRODUCTS")).isTrue();
        assertThat(tableExists("TBL_FLOWS")).isTrue();
        assertThat(tableExists("TBL_CONFIGS")).isTrue();
        assertThat(tableExists("TBL_VERSIONS")).isTrue();
        assertThat(tableExists("TBL_INSTANCES")).isTrue();
        assertThat(tableExists("TBL_LOGS")).isTrue();
        assertThat(tableExists("TBL_BPMN_ELEMENT_GROUP")).isTrue();
        assertThat(tableExists("TBL_BPMN_ELEMENT")).isTrue();
        assertThat(tableExists("TBL_BPMN_GROUP_ELEMENT")).isTrue();
        assertThat(tableExists("TBL_PORTAL_OWNER")).isTrue();
        assertThat(tableExists("TBL_PORTAL_CONFIGURATION")).isTrue();
        assertThat(tableExists("TBL_OWNER_BPMN_GROUP")).isTrue();

        assertThat(sequenceExists("USER_SEQ")).isTrue();
        assertThat(sequenceExists("AUTH_SEQ")).isTrue();
        assertThat(sequenceExists("RESRC_SEQ")).isTrue();
        assertThat(sequenceExists("RES_AUTH_SEQ")).isTrue();
        assertThat(sequenceExists("BPMN_ELEMENT_GROUPS_SEQ")).isTrue();
        assertThat(sequenceExists("BPMN_ELEMENTS_SEQ")).isTrue();
        assertThat(sequenceExists("PORTAL_OWNER_SEQ")).isTrue();

        assertThat(authorityRepository.findByName("ROLE_USER")).isPresent();
        assertThat(authorityRepository.findByName("ROLE_ADMIN")).isPresent();
        assertThat(medAuthorityRepository.findByNameIn(List.of("ROLE_USER", "ROLE_ADMIN"))).hasSize(2);
        assertThat(resourceRepository.count()).isGreaterThan(0);

        Integer liquibaseItUsers = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM jhi_user WHERE login = ?",
            Integer.class,
            "liquibaseit"
        );
        assertThat(liquibaseItUsers).isEqualTo(1);
        assertThat(userRepository.findOneByLogin("liquibaseit")).isPresent();
    }

    @Test
    void activeMediationOwnerHasVueParityBpmnCapabilities() {
        Integer activeMediationOwners = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
              FROM TBL_PORTAL_CONFIGURATION c
              JOIN TBL_PORTAL_OWNER o
                ON o.owner_key = c.active_owner_key
             WHERE c.config_key = 1
               AND UPPER(o.owner_code) = 'MEDIATION'
               AND o.enabled = 1
            """,
            Integer.class
        );
        assertThat(activeMediationOwners).isEqualTo(1);

        List<String> ownerGroups = jdbcTemplate.queryForList(
            """
            SELECT g.group_code
              FROM TBL_PORTAL_CONFIGURATION c
              JOIN TBL_PORTAL_OWNER o
                ON o.owner_key = c.active_owner_key
              JOIN TBL_OWNER_BPMN_GROUP og
                ON og.owner_key = o.owner_key
              JOIN TBL_BPMN_ELEMENT_GROUP g
                ON g.group_key = og.group_key
             WHERE c.config_key = 1
               AND o.enabled = 1
               AND og.enabled = 1
               AND g.enabled = 1
             ORDER BY g.group_code
            """,
            String.class
        );
        assertThat(ownerGroups).containsExactly("CORE_BPMN", "FILE_PROCESSING");

        List<String> elementCodes = jdbcTemplate.query(
            """
            SELECT DISTINCT e.element_code, e.sort_order
              FROM TBL_PORTAL_CONFIGURATION c
              JOIN TBL_PORTAL_OWNER o
                ON o.owner_key = c.active_owner_key
              JOIN TBL_OWNER_BPMN_GROUP og
                ON og.owner_key = o.owner_key
              JOIN TBL_BPMN_ELEMENT_GROUP g
                ON g.group_key = og.group_key
              JOIN TBL_BPMN_GROUP_ELEMENT ge
                ON ge.group_key = g.group_key
              JOIN TBL_BPMN_ELEMENT e
                ON e.element_key = ge.element_key
             WHERE c.config_key = 1
               AND o.enabled = 1
               AND og.enabled = 1
               AND g.enabled = 1
               AND ge.enabled = 1
               AND e.enabled = 1
             ORDER BY e.sort_order, e.element_code
            """,
            (rs, rowNum) -> rs.getString("element_code")
        );
        assertThat(elementCodes).containsExactly(
            "BPMN_START_EVENT",
            "BPMN_END_EVENT",
            "MERGER",
            "FRAGMENTER",
            "FILE_RECEIVER",
            "FILE_TRANSMITTER",
            "CDR_PARSER",
            "CSV_TRANSFORMER"
        );
    }

    @Test
    void loginAndJwtWorkAgainstLiquibaseSchema() throws Exception {
        doNothing().when(captchaValidationService).validate(anyString(), anyString(), anyString());

        LoginVM login = new LoginVM();
        login.setUsername("liquibaseit");
        login.setPassword("user");
        login.setRememberMe(false);
        login.setCaptchaId(TEST_CAPTCHA_ID);
        login.setCaptchaToken(TEST_CAPTCHA_TOKEN);

        String token = mockMvc
            .perform(post("/api/authenticate").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(login)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id_token").isString())
            .andExpect(header().string(JWTFilter.AUTHORIZATION_HEADER, org.hamcrest.Matchers.not(org.hamcrest.Matchers.emptyOrNullString())))
            .andReturn()
            .getResponse()
            .getHeader(JWTFilter.AUTHORIZATION_HEADER);

        assertThat(token).startsWith("Bearer ");

        mockMvc
            .perform(get("/api/account").header(JWTFilter.AUTHORIZATION_HEADER, token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.login").value("liquibaseit"));
    }

    @Test
    void liquibaseSecondRunIsIdempotent() throws Exception {
        List<String> before = changeSetIds();
        assertThat(before).isNotEmpty();

        try (Connection connection = dataSource.getConnection()) {
            Liquibase liquibase = new Liquibase(
                "config/liquibase/master.xml",
                new ClassLoaderResourceAccessor(),
                DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection))
            );
            liquibase.update("test");
        }

        List<String> after = changeSetIds();
        assertThat(after).containsExactlyElementsOf(before);
    }

    private List<String> changeSetIds() {
        return jdbcTemplate.query(
            "SELECT ID FROM DATABASECHANGELOG ORDER BY ORDEREXECUTED",
            (rs, rowNum) -> rs.getString(1)
        );
    }

    private boolean tableExists(String table) throws Exception {
        try (Connection c = dataSource.getConnection(); ResultSet rs = c.getMetaData().getTables(null, null, table, new String[] { "TABLE" })) {
            if (rs.next()) {
                return true;
            }
        }
        try (Connection c = dataSource.getConnection(); ResultSet rs = c.getMetaData().getTables(null, null, table.toLowerCase(), new String[] { "TABLE" })) {
            return rs.next();
        }
    }

    private boolean columnExists(String table, String column) throws Exception {
        try (Connection c = dataSource.getConnection(); ResultSet rs = c.getMetaData().getColumns(null, null, table, column)) {
            if (rs.next()) {
                return true;
            }
        }
        try (
            Connection c = dataSource.getConnection();
            ResultSet rs = c.getMetaData().getColumns(null, null, table.toLowerCase(), column.toLowerCase())
        ) {
            return rs.next();
        }
    }

    private boolean sequenceExists(String sequence) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM USER_SEQUENCES WHERE SEQUENCE_NAME = ?",
            Integer.class,
            sequence.toUpperCase()
        );
        return count != null && count > 0;
    }
}
