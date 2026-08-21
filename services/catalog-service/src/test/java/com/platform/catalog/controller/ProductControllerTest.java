//package com.platform.catalog.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.platform.catalog.dto.product.ProductCreateRequest;
//import com.platform.catalog.dto.product.ProductResponse;
//import com.platform.catalog.entity.enums.ProductStatus;
//import com.platform.catalog.security.JwtAuthenticationFilter;
//import com.platform.catalog.service.ProductService;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.time.Instant;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(ProductController.class)
//@Import(com.platform.catalog.config.SecurityConfig.class)
//class ProductControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockitoBean
//    private ProductService productService;
//
//    @MockitoBean
//    private JwtAuthenticationFilter jwtAuthenticationFilter;
//
//    @Test
//    @WithMockUser(roles = "CUSTOMER")
//    void shouldGetProductById() throws Exception {
//
//        ProductResponse response =
//                new ProductResponse(
//                        1L,
//                        1L,
//                        "Masala Powders",
//                        "Biryani Masala",
//                        "biryani-masala",
//                        "Premium masala",
//                        "MAS-BIR-001",
//                        ProductStatus.ACTIVE,
//                        Instant.now(),
//                        Instant.now()
//                );
//
//        Mockito.when(
//                productService.findById(1L)
//        ).thenReturn(response);
//
//        mockMvc.perform(
//                        get("/api/v1/products/1")
//                )
//                .andExpect(status().isOk())
//                .andExpect(
//                        jsonPath("$.id")
//                                .value(1)
//                )
//                .andExpect(
//                        jsonPath("$.name")
//                                .value("Biryani Masala")
//                )
//                .andExpect(
//                        jsonPath("$.sku")
//                                .value("MAS-BIR-001")
//                );
//    }
//
//    @Test
//    @WithMockUser(roles = "CUSTOMER")
//    void customerCanReadProduct() throws Exception {
//
//        ProductResponse response =
//                new ProductResponse(
//                        1L,
//                        1L,
//                        "Masala Powders",
//                        "Biryani Masala",
//                        "biryani-masala",
//                        "Premium masala",
//                        "MAS-BIR-001",
//                        ProductStatus.ACTIVE,
//                        Instant.now(),
//                        Instant.now()
//                );
//
//        Mockito.when(
//                productService.findById(1L)
//        ).thenReturn(response);
//
//        mockMvc.perform(
//                        get("/api/v1/products/1")
//                )
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    @WithMockUser(roles = "CUSTOMER")
//    void customerCannotCreateProduct() throws Exception {
//
//        ProductCreateRequest request =
//                new ProductCreateRequest(
//                        1L,
//                        "Test Product",
//                        "test-product",
//                        "Test description",
//                        "TEST-001"
//                );
//
//        mockMvc.perform(
//                        post("/api/v1/products")
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(
//                                        objectMapper.writeValueAsString(
//                                                request
//                                        )
//                                )
//                )
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    @WithMockUser(roles = "ADMIN")
//    void adminCanCreateProduct() throws Exception {
//
//        ProductCreateRequest request =
//                new ProductCreateRequest(
//                        1L,
//                        "Biryani Masala",
//                        "biryani-masala",
//                        "Premium masala",
//                        "MAS-BIR-001"
//                );
//
//        ProductResponse response =
//                new ProductResponse(
//                        1L,
//                        1L,
//                        "Masala Powders",
//                        "Biryani Masala",
//                        "biryani-masala",
//                        "Premium masala",
//                        "MAS-BIR-001",
//                        ProductStatus.ACTIVE,
//                        Instant.now(),
//                        Instant.now()
//                );
//
//        Mockito.when(
//                productService.create(any(ProductCreateRequest.class))
//        ).thenReturn(response);
//
//        mockMvc.perform(
//                        post("/api/v1/products")
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(
//                                        objectMapper.writeValueAsString(
//                                                request
//                                        )
//                                )
//                )
//                .andExpect(status().isCreated())
//                .andExpect(
//                        jsonPath("$.name")
//                                .value("Biryani Masala")
//                );
//    }
//}