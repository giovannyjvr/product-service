package com.insper.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insper.product.model.Product;
import com.insper.product.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = ProductController.class)
@AutoConfigureMockMvc
class ProductControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Nested
    @DisplayName("GET /products")
    class ListAllProducts {

        @Test
        @DisplayName("Deve retornar lista vazia quando não houver produtos")
        void getAll_emptyList() throws Exception {
            given(productService.findAll()).willReturn(Arrays.asList());

            mockMvc.perform(get("/products"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Deve retornar lista com produtos quando existirem no banco")
        void getAll_withProducts() throws Exception {
            Product p1 = new Product();
            p1.setId(UUID.randomUUID());
            p1.setName("Caneta");
            p1.setPrice(2.50);
            p1.setUnit("un");

            Product p2 = new Product();
            p2.setId(UUID.randomUUID());
            p2.setName("Caderno");
            p2.setPrice(15.00);
            p2.setUnit("un");

            given(productService.findAll()).willReturn(Arrays.asList(p1, p2));

            mockMvc.perform(get("/products"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id", is(p1.getId().toString())))
                    .andExpect(jsonPath("$[0].name", is("Caneta")))
                    .andExpect(jsonPath("$[0].price", is(2.50)))
                    .andExpect(jsonPath("$[0].unit", is("un")))
                    .andExpect(jsonPath("$[1].name", is("Caderno")));
        }
    }

    @Nested
    @DisplayName("GET /products/{id}")
    class GetProductById {

        @Test
        @DisplayName("Deve retornar 404 quando produto não existir")
        void getById_notFound() throws Exception {
            UUID randomId = UUID.randomUUID();
            given(productService.findById(randomId)).willReturn(Optional.empty());

            mockMvc.perform(get("/products/{id}", randomId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar produto quando existir")
        void getById_found() throws Exception {
            UUID randomId = UUID.randomUUID();
            Product p = new Product();
            p.setId(randomId);
            p.setName("Lápis");
            p.setPrice(1.20);
            p.setUnit("un");

            given(productService.findById(randomId)).willReturn(Optional.of(p));

            mockMvc.perform(get("/products/{id}", randomId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(randomId.toString())))
                    .andExpect(jsonPath("$.name", is("Lápis")))
                    .andExpect(jsonPath("$.price", is(1.20)))
                    .andExpect(jsonPath("$.unit", is("un")));
        }
    }

    @Nested
    @DisplayName("POST /products")
    class CreateProduct {

        @Test
        @DisplayName("Deve recusar criação sem autenticação")
        void create_unauthenticated() throws Exception {
            Product p = new Product();
            p.setName("Borracha");
            p.setPrice(0.80);
            p.setUnit("un");

            String json = mapper.writeValueAsString(p);

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Deve criar produto quando usuário for ADMIN")
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        void create_asAdmin() throws Exception {
            Product p = new Product();
            p.setName("Borracha");
            p.setPrice(0.80);
            p.setUnit("un");

            Product saved = new Product();
            saved.setId(UUID.randomUUID());
            saved.setName("Borracha");
            saved.setPrice(0.80);
            saved.setUnit("un");

            given(productService.save(ArgumentMatchers.any(Product.class))).willReturn(saved);

            String json = mapper.writeValueAsString(p);

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/products/" + saved.getId().toString())))
                    .andExpect(jsonPath("$.id", is(saved.getId().toString())))
                    .andExpect(jsonPath("$.name", is("Borracha")))
                    .andExpect(jsonPath("$.price", is(0.80)))
                    .andExpect(jsonPath("$.unit", is("un")));
        }
    }

    @Nested
    @DisplayName("PUT /products/{id}")
    class UpdateProduct {

        @Test
        @DisplayName("Deve retornar 404 ao atualizar produto inexistente")
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        void update_notFound() throws Exception {
            UUID randomId = UUID.randomUUID();
            Product p = new Product();
            p.setName("Agenda");
            p.setPrice(25.00);
            p.setUnit("un");

            given(productService.findById(randomId)).willReturn(Optional.empty());

            String json = mapper.writeValueAsString(p);

            mockMvc.perform(put("/products/{id}", randomId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve atualizar produto quando existir e usuário for ADMIN")
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        void update_success() throws Exception {
            UUID randomId = UUID.randomUUID();
            Product existing = new Product();
            existing.setId(randomId);
            existing.setName("Agenda");
            existing.setPrice(20.00);
            existing.setUnit("un");

            Product updatedInput = new Product();
            updatedInput.setName("Agenda Premium");
            updatedInput.setPrice(30.00);
            updatedInput.setUnit("un");

            Product updatedSaved = new Product();
            updatedSaved.setId(randomId);
            updatedSaved.setName("Agenda Premium");
            updatedSaved.setPrice(30.00);
            updatedSaved.setUnit("un");

            given(productService.findById(randomId)).willReturn(Optional.of(existing));
            given(productService.save(ArgumentMatchers.any(Product.class))).willReturn(updatedSaved);

            String json = mapper.writeValueAsString(updatedInput);

            mockMvc.perform(put("/products/{id}", randomId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(randomId.toString())))
                    .andExpect(jsonPath("$.name", is("Agenda Premium")))
                    .andExpect(jsonPath("$.price", is(30.00)))
                    .andExpect(jsonPath("$.unit", is("un")));
        }
    }

    @Nested
    @DisplayName("DELETE /products/{id}")
    class DeleteProduct {

        @Test
        @DisplayName("Deve retornar 404 ao deletar produto inexistente")
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        void delete_notFound() throws Exception {
            UUID randomId = UUID.randomUUID();
            given(productService.findById(randomId)).willReturn(Optional.empty());

            mockMvc.perform(delete("/products/{id}", randomId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve deletar produto quando existir e usuário for ADMIN")
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        void delete_success() throws Exception {
            UUID randomId = UUID.randomUUID();
            Product existing = new Product();
            existing.setId(randomId);
            existing.setName("Marcador");
            existing.setPrice(5.00);
            existing.setUnit("un");

            given(productService.findById(randomId)).willReturn(Optional.of(existing));
            doNothing().when(productService).deleteById(randomId);

            mockMvc.perform(delete("/products/{id}", randomId))
                    .andExpect(status().isNoContent());
        }
    }
}
