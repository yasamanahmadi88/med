package com.behsa.medportal.web.rest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.config.BpmnOwnerSchema;
import com.behsa.medportal.domain.BpmnElementEntity;
import com.behsa.medportal.domain.BpmnElementGroupEntity;
import com.behsa.medportal.repository.BpmnElementGroupRepository;
import com.behsa.medportal.repository.BpmnElementRepository;
import com.behsa.medportal.security.AuthoritiesConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class BpmnElementAccessResourceIT {

    private static final String API_URL =
        "/api/bpmn-element-access/current";

    private static final long OWNER_ID = 91001L;

    @Autowired
    private MockMvc mockMvc;

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
            "Owner access integration test",
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
    @WithMockUser(authorities = AuthoritiesConstants.USER)
    void shouldReturnMappedOwnerAccessForAuthorizedUser()
        throws Exception {

        BpmnElementGroupEntity group = saveGroup(
            "FILE_PROCESSING",
            "File Processing",
            1
        );

        BpmnElementEntity merger = saveElement(
            "MERGER",
            "Merger:Merger",
            "Merger",
            "merger",
            "create.merger",
            "Merger",
            1,
            10
        );

        BpmnElementEntity fileReceiver = saveElement(
            "FILE_RECEIVER",
            "FileReceiver:FileReceiver",
            "FileReceiver",
            "fileReceiver",
            "create.file-receiver",
            "File Receiver",
            1,
            20
        );

        assignGroupToOwner(group, 1);
        assignElementToGroup(group, merger);
        assignElementToGroup(group, fileReceiver);

        mockMvc
            .perform(get(API_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ownerCode").value("MEDIATION"))
            .andExpect(
                jsonPath("$.ownerDisplayName")
                    .value("Mediation Portal")
            )
            .andExpect(jsonPath("$.productId").doesNotExist())
            .andExpect(jsonPath("$.productName").doesNotExist())
            .andExpect(jsonPath("$.groups.length()").value(1))
            .andExpect(
                jsonPath("$.groups[0].code")
                    .value("FILE_PROCESSING")
            )
            .andExpect(
                jsonPath("$.groups[0].name")
                    .value("File Processing")
            )
            .andExpect(jsonPath("$.elements.length()").value(2))
            .andExpect(
                jsonPath("$.elements[0].code")
                    .value("MERGER")
            )
            .andExpect(
                jsonPath("$.elements[0].bpmnType")
                    .value("Merger:Merger")
            )
            .andExpect(
                jsonPath("$.elements[0].namespaceUri")
                    .value("Merger")
            )
            .andExpect(
                jsonPath("$.elements[0].localName")
                    .value("merger")
            )
            .andExpect(
                jsonPath("$.elements[0].paletteAction")
                    .value("create.merger")
            )
            .andExpect(
                jsonPath("$.elements[0].displayName")
                    .value("Merger")
            )
            .andExpect(
                jsonPath("$.elements[0].sortOrder")
                    .value(10)
            )
            .andExpect(
                jsonPath("$.elements[1].code")
                    .value("FILE_RECEIVER")
            )
            .andExpect(
                jsonPath("$.elements[1].sortOrder")
                    .value(20)
            );
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.USER)
    void shouldReturnEmptyAccessWhenOwnerHasNoGroupMapping()
        throws Exception {

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
         * Catalog configuration exists, but the active Owner has
         * deliberately not been assigned this group.
         */
        assignElementToGroup(group, element);

        mockMvc
            .perform(get(API_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ownerCode").value("MEDIATION"))
            .andExpect(jsonPath("$.groups").isEmpty())
            .andExpect(jsonPath("$.elements").isEmpty());
    }

    @Test
    @WithMockUser(authorities = "ROLE_DENIED")
    void shouldReturnForbiddenWhenUserHasNoFlowViewPermission()
        throws Exception {

        mockMvc
            .perform(get(API_URL))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedForUnauthenticatedUser()
        throws Exception {

        mockMvc
            .perform(
                get(API_URL)
                    .with(anonymous())
            )
            .andExpect(status().isUnauthorized());
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
            OWNER_ID,
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