package hu.blogplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.blogplatform.dto.AuthDtos;
import hu.blogplatform.dto.PostDtos;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integrációs teszt a teljes Spring Boot kontextus indításával,
 * mely végigköveti a regisztráció → bejegyzés létrehozás → listázás folyamatot.
 */
@SpringBootTest
@ActiveProfiles("test")
class PostApiIntegrationTest {

    @Autowired private WebApplicationContext context;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private FilterChainProxy springSecurityFilterChain;

    private MockMvc mockMvc;

    @PostConstruct
    void initMockMvc() {
        // A Spring Security szervlet szűrőláncának hozzáadása, hogy a JWT filter is fusson.
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @Test
    void fullFlow_registerCreatePostAndList() throws Exception {
        // 1) Regisztráció
        AuthDtos.RegisterRequest reg = new AuthDtos.RegisterRequest(
                "integUser", "integ@example.com", "jelszo123");

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        AuthDtos.AuthResponse authResponse = objectMapper.readValue(
                regResult.getResponse().getContentAsString(), AuthDtos.AuthResponse.class);
        String token = authResponse.token();
        assertThat(token).isNotBlank();

        // 2) Bejegyzés létrehozás autentikációval
        PostDtos.PostRequest postReq = new PostDtos.PostRequest(
                "Integrációs cím", "Integrációs tartalom");

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Integrációs cím"))
                .andExpect(jsonPath("$.authorUsername").value("integUser"));

        // 3) Listázás publikusan
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Integrációs cím"));
    }

    @Test
    void createPost_withoutAuth_returns401or403() throws Exception {
        PostDtos.PostRequest postReq = new PostDtos.PostRequest("Cím", "Tartalom");

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postReq)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertThat(status).isIn(401, 403);
                });
    }
}
