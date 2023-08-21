package com.behsa.medportal.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.behsa.medportal.IntegrationTest;
import com.behsa.medportal.domain.FlowEntity;
import com.behsa.medportal.domain.ProductEntity;
import com.behsa.medportal.repository.ProductRepository;
import com.behsa.medportal.service.dto.ProductDTO;
import com.behsa.medportal.service.mapper.ProductMapper;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ProductResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ProductResourceIT {

    private static final String DEFAULT_PRODUCT_NAME = "AAA";
    private static final String UPDATED_PRODUCT_NAME = "BBB";

    private static final String DEFAULT_PRODUCT_DESC = "AAAAAAAAAA";
    private static final String UPDATED_PRODUCT_DESC = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/products";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restProductMockMvc;

    private ProductEntity productEntity;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ProductEntity createEntity(EntityManager em) {
        ProductEntity productEntity = new ProductEntity().productName(DEFAULT_PRODUCT_NAME).productDesc(DEFAULT_PRODUCT_DESC);
        return productEntity;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ProductEntity createUpdatedEntity(EntityManager em) {
        ProductEntity productEntity = new ProductEntity().productName(UPDATED_PRODUCT_NAME).productDesc(UPDATED_PRODUCT_DESC);
        return productEntity;
    }

    @BeforeEach
    public void initTest() {
        productEntity = createEntity(em);
    }

    @Test
    @Transactional
    void createProduct() throws Exception {
        int databaseSizeBeforeCreate = productRepository.findAll().size();
        // Create the Product
        ProductDTO productDTO = productMapper.toDto(productEntity);
        restProductMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(productDTO)))
            .andExpect(status().isCreated());

        // Validate the Product in the database
        List<ProductEntity> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeCreate + 1);
        ProductEntity testProduct = productList.get(productList.size() - 1);
        assertThat(testProduct.getProductName()).isEqualTo(DEFAULT_PRODUCT_NAME);
        assertThat(testProduct.getProductDesc()).isEqualTo(DEFAULT_PRODUCT_DESC);
    }

    @Test
    @Transactional
    void createProductWithExistingId() throws Exception {
        // Create the Product with an existing ID
        productEntity.setId(1L);
        ProductDTO productDTO = productMapper.toDto(productEntity);

        int databaseSizeBeforeCreate = productRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restProductMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(productDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Product in the database
        List<ProductEntity> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkProductNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = productRepository.findAll().size();
        // set the field null
        productEntity.setProductName(null);

        // Create the Product, which fails.
        ProductDTO productDTO = productMapper.toDto(productEntity);

        restProductMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(productDTO)))
            .andExpect(status().isBadRequest());

        List<ProductEntity> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkProductDescIsRequired() throws Exception {
        int databaseSizeBeforeTest = productRepository.findAll().size();
        // set the field null
        productEntity.setProductDesc(null);

        // Create the Product, which fails.
        ProductDTO productDTO = productMapper.toDto(productEntity);

        restProductMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(productDTO)))
            .andExpect(status().isBadRequest());

        List<ProductEntity> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllProducts() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList
        restProductMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(productEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].productName").value(hasItem(DEFAULT_PRODUCT_NAME)))
            .andExpect(jsonPath("$.[*].productDesc").value(hasItem(DEFAULT_PRODUCT_DESC)));
    }

    @Test
    @Transactional
    void getProduct() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get the product
        restProductMockMvc
            .perform(get(ENTITY_API_URL_ID, productEntity.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(productEntity.getId().intValue()))
            .andExpect(jsonPath("$.productName").value(DEFAULT_PRODUCT_NAME))
            .andExpect(jsonPath("$.productDesc").value(DEFAULT_PRODUCT_DESC));
    }

    @Test
    @Transactional
    void getProductsByIdFiltering() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        Long id = productEntity.getId();

        defaultProductShouldBeFound("id.equals=" + id);
        defaultProductShouldNotBeFound("id.notEquals=" + id);

        defaultProductShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultProductShouldNotBeFound("id.greaterThan=" + id);

        defaultProductShouldBeFound("id.lessThanOrEqual=" + id);
        defaultProductShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllProductsByProductNameIsEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where productName equals to DEFAULT_PRODUCT_NAME
        defaultProductShouldBeFound("productName.equals=" + DEFAULT_PRODUCT_NAME);

        // Get all the productList where productName equals to UPDATED_PRODUCT_NAME
        defaultProductShouldNotBeFound("productName.equals=" + UPDATED_PRODUCT_NAME);
    }

    @Test
    @Transactional
    void getAllProductsByProductNameIsInShouldWork() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where productName in DEFAULT_PRODUCT_NAME or UPDATED_PRODUCT_NAME
        defaultProductShouldBeFound("productName.in=" + DEFAULT_PRODUCT_NAME + "," + UPDATED_PRODUCT_NAME);

        // Get all the productList where productName equals to UPDATED_PRODUCT_NAME
        defaultProductShouldNotBeFound("productName.in=" + UPDATED_PRODUCT_NAME);
    }

    @Test
    @Transactional
    void getAllProductsByProductNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where productName is not null
        defaultProductShouldBeFound("productName.specified=true");

        // Get all the productList where productName is null
        defaultProductShouldNotBeFound("productName.specified=false");
    }

    @Test
    @Transactional
    void getAllProductsByProductNameContainsSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where productName contains DEFAULT_PRODUCT_NAME
        defaultProductShouldBeFound("productName.contains=" + DEFAULT_PRODUCT_NAME);

        // Get all the productList where productName contains UPDATED_PRODUCT_NAME
        defaultProductShouldNotBeFound("productName.contains=" + UPDATED_PRODUCT_NAME);
    }

    @Test
    @Transactional
    void getAllProductsByProductNameNotContainsSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where productName does not contain DEFAULT_PRODUCT_NAME
        defaultProductShouldNotBeFound("productName.doesNotContain=" + DEFAULT_PRODUCT_NAME);

        // Get all the productList where productName does not contain UPDATED_PRODUCT_NAME
        defaultProductShouldBeFound("productName.doesNotContain=" + UPDATED_PRODUCT_NAME);
    }

    @Test
    @Transactional
    void getAllProductsByProductDescIsEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where productDesc equals to DEFAULT_PRODUCT_DESC
        defaultProductShouldBeFound("productDesc.equals=" + DEFAULT_PRODUCT_DESC);

        // Get all the productList where productDesc equals to UPDATED_PRODUCT_DESC
        defaultProductShouldNotBeFound("productDesc.equals=" + UPDATED_PRODUCT_DESC);
    }

    @Test
    @Transactional
    void getAllProductsByProductDescIsInShouldWork() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where productDesc in DEFAULT_PRODUCT_DESC or UPDATED_PRODUCT_DESC
        defaultProductShouldBeFound("productDesc.in=" + DEFAULT_PRODUCT_DESC + "," + UPDATED_PRODUCT_DESC);

        // Get all the productList where productDesc equals to UPDATED_PRODUCT_DESC
        defaultProductShouldNotBeFound("productDesc.in=" + UPDATED_PRODUCT_DESC);
    }

    @Test
    @Transactional
    void getAllProductsByProductDescIsNullOrNotNull() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where productDesc is not null
        defaultProductShouldBeFound("productDesc.specified=true");

        // Get all the productList where productDesc is null
        defaultProductShouldNotBeFound("productDesc.specified=false");
    }

    @Test
    @Transactional
    void getAllProductsByProductDescContainsSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where productDesc contains DEFAULT_PRODUCT_DESC
        defaultProductShouldBeFound("productDesc.contains=" + DEFAULT_PRODUCT_DESC);

        // Get all the productList where productDesc contains UPDATED_PRODUCT_DESC
        defaultProductShouldNotBeFound("productDesc.contains=" + UPDATED_PRODUCT_DESC);
    }

    @Test
    @Transactional
    void getAllProductsByProductDescNotContainsSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where productDesc does not contain DEFAULT_PRODUCT_DESC
        defaultProductShouldNotBeFound("productDesc.doesNotContain=" + DEFAULT_PRODUCT_DESC);

        // Get all the productList where productDesc does not contain UPDATED_PRODUCT_DESC
        defaultProductShouldBeFound("productDesc.doesNotContain=" + UPDATED_PRODUCT_DESC);
    }

    @Test
    @Transactional
    void getAllProductsByFlowsIsEqualToSomething() throws Exception {
        FlowEntity flows;
        if (TestUtil.findAll(em, FlowEntity.class).isEmpty()) {
            productRepository.saveAndFlush(productEntity);
            flows = FlowResourceIT.createEntity(em);
        } else {
            flows = TestUtil.findAll(em, FlowEntity.class).get(0);
        }
        em.persist(flows);
        em.flush();
        productEntity.addFlows(flows);
        productRepository.saveAndFlush(productEntity);
        Long flowsId = flows.getId();

        // Get all the productList where flows equals to flowsId
        defaultProductShouldBeFound("flowsId.equals=" + flowsId);

        // Get all the productList where flows equals to (flowsId + 1)
        defaultProductShouldNotBeFound("flowsId.equals=" + (flowsId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultProductShouldBeFound(String filter) throws Exception {
        restProductMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(productEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].productName").value(hasItem(DEFAULT_PRODUCT_NAME)))
            .andExpect(jsonPath("$.[*].productDesc").value(hasItem(DEFAULT_PRODUCT_DESC)));

        // Check, that the count call also returns 1
        restProductMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultProductShouldNotBeFound(String filter) throws Exception {
        restProductMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restProductMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingProduct() throws Exception {
        // Get the product
        restProductMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingProduct() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        int databaseSizeBeforeUpdate = productRepository.findAll().size();

        // Update the product
        ProductEntity updatedProductEntity = productRepository.findById(productEntity.getId()).get();
        // Disconnect from session so that the updates on updatedProductEntity are not directly saved in db
        em.detach(updatedProductEntity);
        updatedProductEntity.productName(UPDATED_PRODUCT_NAME).productDesc(UPDATED_PRODUCT_DESC);
        ProductDTO productDTO = productMapper.toDto(updatedProductEntity);

        restProductMockMvc
            .perform(
                put(ENTITY_API_URL_ID, productDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(productDTO))
            )
            .andExpect(status().isOk());

        // Validate the Product in the database
        List<ProductEntity> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeUpdate);
        ProductEntity testProduct = productList.get(productList.size() - 1);
        assertThat(testProduct.getProductName()).isEqualTo(UPDATED_PRODUCT_NAME);
        assertThat(testProduct.getProductDesc()).isEqualTo(UPDATED_PRODUCT_DESC);
    }

    @Test
    @Transactional
    void putNonExistingProduct() throws Exception {
        int databaseSizeBeforeUpdate = productRepository.findAll().size();
        productEntity.setId(count.incrementAndGet());

        // Create the Product
        ProductDTO productDTO = productMapper.toDto(productEntity);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProductMockMvc
            .perform(
                put(ENTITY_API_URL_ID, productDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(productDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Product in the database
        List<ProductEntity> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchProduct() throws Exception {
        int databaseSizeBeforeUpdate = productRepository.findAll().size();
        productEntity.setId(count.incrementAndGet());

        // Create the Product
        ProductDTO productDTO = productMapper.toDto(productEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(productDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Product in the database
        List<ProductEntity> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamProduct() throws Exception {
        int databaseSizeBeforeUpdate = productRepository.findAll().size();
        productEntity.setId(count.incrementAndGet());

        // Create the Product
        ProductDTO productDTO = productMapper.toDto(productEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(productDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Product in the database
        List<ProductEntity> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateProductWithPatch() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        int databaseSizeBeforeUpdate = productRepository.findAll().size();

        // Update the product using partial update
        ProductEntity partialUpdatedProductEntity = new ProductEntity();
        partialUpdatedProductEntity.setId(productEntity.getId());

        restProductMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProductEntity.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedProductEntity))
            )
            .andExpect(status().isOk());

        // Validate the Product in the database
        List<ProductEntity> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeUpdate);
        ProductEntity testProduct = productList.get(productList.size() - 1);
        assertThat(testProduct.getProductName()).isEqualTo(DEFAULT_PRODUCT_NAME);
        assertThat(testProduct.getProductDesc()).isEqualTo(DEFAULT_PRODUCT_DESC);
    }

    @Test
    @Transactional
    void fullUpdateProductWithPatch() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        int databaseSizeBeforeUpdate = productRepository.findAll().size();

        // Update the product using partial update
        ProductEntity partialUpdatedProductEntity = new ProductEntity();
        partialUpdatedProductEntity.setId(productEntity.getId());

        partialUpdatedProductEntity.productName(UPDATED_PRODUCT_NAME).productDesc(UPDATED_PRODUCT_DESC);

        restProductMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProductEntity.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedProductEntity))
            )
            .andExpect(status().isOk());

        // Validate the Product in the database
        List<ProductEntity> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeUpdate);
        ProductEntity testProduct = productList.get(productList.size() - 1);
        assertThat(testProduct.getProductName()).isEqualTo(UPDATED_PRODUCT_NAME);
        assertThat(testProduct.getProductDesc()).isEqualTo(UPDATED_PRODUCT_DESC);
    }

    @Test
    @Transactional
    void patchNonExistingProduct() throws Exception {
        int databaseSizeBeforeUpdate = productRepository.findAll().size();
        productEntity.setId(count.incrementAndGet());

        // Create the Product
        ProductDTO productDTO = productMapper.toDto(productEntity);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProductMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, productDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(productDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Product in the database
        List<ProductEntity> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchProduct() throws Exception {
        int databaseSizeBeforeUpdate = productRepository.findAll().size();
        productEntity.setId(count.incrementAndGet());

        // Create the Product
        ProductDTO productDTO = productMapper.toDto(productEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(productDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Product in the database
        List<ProductEntity> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamProduct() throws Exception {
        int databaseSizeBeforeUpdate = productRepository.findAll().size();
        productEntity.setId(count.incrementAndGet());

        // Create the Product
        ProductDTO productDTO = productMapper.toDto(productEntity);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(productDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Product in the database
        List<ProductEntity> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteProduct() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        int databaseSizeBeforeDelete = productRepository.findAll().size();

        // Delete the product
        restProductMockMvc
            .perform(delete(ENTITY_API_URL_ID, productEntity.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<ProductEntity> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
