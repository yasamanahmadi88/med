package com.behsa.medportal.web.rest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.domain.BpmnElementEntity;
import com.behsa.medportal.domain.BpmnElementGroupEntity;
import com.behsa.medportal.domain.ProductEntity;
import com.behsa.medportal.repository.BpmnElementGroupRepository;
import com.behsa.medportal.repository.BpmnElementRepository;
import com.behsa.medportal.repository.ProductRepository;
import com.behsa.medportal.security.AuthoritiesConstants;
import org.junit.jupiter.api.BeforeAll;
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

    private static final String API_URL = "/api/bpmn-element-access/products/{productId}";

    @Autowired
    private MockMvc mockMvc;

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
            CREATE TABLE IF NOT EXISTS TBL_PRODUCT_BPMN_GROUP (
                product_key BIGINT NOT NULL,
                group_key BIGINT NOT NULL,
                PRIMARY KEY (product_key, group_key)
            )
            """
        );
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.USER)
    void shouldReturnMappedProductAccessForAuthorizedUser() throws Exception {
        ProductEntity product = saveProduct("MCI");

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

        assignGroupToProduct(product, group);
        assignElementToGroup(group, merger);
        assignElementToGroup(group, fileReceiver);

        mockMvc
            .perform(get(API_URL, product.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(product.getId()))
            .andExpect(jsonPath("$.productName").value("MCI"))
            .andExpect(jsonPath("$.groups.length()").value(1))
            .andExpect(jsonPath("$.groups[0].code").value("FILE_PROCESSING"))
            .andExpect(jsonPath("$.groups[0].name").value("File Processing"))
            .andExpect(jsonPath("$.elements.length()").value(2))
            .andExpect(jsonPath("$.elements[0].code").value("MERGER"))
            .andExpect(jsonPath("$.elements[0].bpmnType").value("Merger:Merger"))
            .andExpect(jsonPath("$.elements[0].namespaceUri").value("Merger"))
            .andExpect(jsonPath("$.elements[0].localName").value("merger"))
            .andExpect(jsonPath("$.elements[0].paletteAction").value("create.merger"))
            .andExpect(jsonPath("$.elements[0].displayName").value("Merger"))
            .andExpect(jsonPath("$.elements[0].sortOrder").value(10))
            .andExpect(jsonPath("$.elements[1].code").value("FILE_RECEIVER"))
            .andExpect(jsonPath("$.elements[1].sortOrder").value(20));
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.USER)
    void shouldReturnEmptyAccessForProductWithoutMapping() throws Exception {
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

        // Catalog configuration exists, but this product deliberately has no group mapping.
        assignElementToGroup(group, element);

        mockMvc
            .perform(get(API_URL, product.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(product.getId()))
            .andExpect(jsonPath("$.productName").value("NMP"))
            .andExpect(jsonPath("$.groups").isEmpty())
            .andExpect(jsonPath("$.elements").isEmpty());
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.USER)
    void shouldReturnBadRequestWhenProductDoesNotExist() throws Exception {
        long missingProductId = Long.MAX_VALUE;

        mockMvc
            .perform(get(API_URL, missingProductId))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("error.productnotfound"))
            .andExpect(jsonPath("$.params").value("flow"))
            .andExpect(jsonPath("$.stackTrace").doesNotExist())
            .andExpect(jsonPath("$.cause").doesNotExist())
            .andExpect(jsonPath("$.suppressed").doesNotExist())
            .andExpect(jsonPath("$.className").doesNotExist())
            .andExpect(jsonPath("$.methodName").doesNotExist())
            .andExpect(
                jsonPath("$.path").value(
                    "/api/bpmn-element-access/products/" + missingProductId
                )
            );
    }

    @Test
    @WithMockUser(authorities = "ROLE_DENIED")
    void shouldReturnForbiddenWhenUserHasNoFlowViewPermission() throws Exception {
        mockMvc
            .perform(get(API_URL, 1L))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedForUnauthenticatedUser() throws Exception {
        mockMvc
            .perform(get(API_URL, 1L).with(anonymous()))
            .andExpect(status().isUnauthorized());
    }

    private ProductEntity saveProduct(String productName) {
        ProductEntity product = new ProductEntity();
        product.setProductName(productName);
        product.setProductDesc("BPMN access resource integration test");

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