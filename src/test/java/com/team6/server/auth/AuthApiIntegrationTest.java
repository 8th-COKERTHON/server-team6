package com.team6.server.auth;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team6.server.auth.repository.MemberRepository;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthApiIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired MemberRepository memberRepository;

    @BeforeEach
    void cleanUp() {
        memberRepository.deleteAll();
    }

    @Test
    void publicApiCanBeUsedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/sample/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void protectedApiRejectsUnauthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/v1/sample/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_401_4"));
    }

    @Test
    void protectedApiRejectsInvalidTokenWithStablePublicCode() throws Exception {
        mockMvc.perform(get("/api/v1/sample/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTH_401_1"));
    }

    @Test
    void invalidSignUpRequestUsesValidationErrorCode() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody("not-an-email", "short", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("GLOBAL_400_2"));
    }

    @Test
    void signUpLoginAndCallProtectedApiWithAccessToken() throws Exception {
        signUp("user@example.com", "password123!", "테스트 사용자");

        JsonNode tokens = login("user@example.com", "password123!");
        String accessToken = tokens.path("data").path("accessToken").asText();

        org.assertj.core.api.Assertions.assertThat(tokens.path("data").path("name").asText())
                .isEqualTo("테스트 사용자");
        org.assertj.core.api.Assertions.assertThat(tokens.path("data").path("email").asText())
                .isEqualTo("user@example.com");

        mockMvc.perform(get("/api/v1/sample/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.memberId", not(blankOrNullString())));
    }

    @Test
    void refreshTokenIssuesNewTokenPair() throws Exception {
        signUp("refresh@example.com", "password123!", "재발급 사용자");
        String refreshToken = login("refresh@example.com", "password123!")
                .path("data").path("refreshToken").asText();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken", not(blankOrNullString())))
                .andExpect(jsonPath("$.data.refreshToken", not(blankOrNullString())));
    }

    @Test
    void duplicateEmailAndWrongPasswordReturnExpectedErrors() throws Exception {
        signUp("duplicate@example.com", "password123!", "사용자");

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody("duplicate@example.com", "password123!", "다른 사용자")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEMBER_409_1"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "duplicate@example.com",
                                "password", "wrong-password"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_401_5"));
    }

    private void signUp(String email, String password, String name) throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody(email, password, name)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.memberId").isNumber());
    }

    private JsonNode login(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken", not(blankOrNullString())))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private String signUpBody(String email, String password, String name) throws Exception {
        return objectMapper.writeValueAsString(Map.of("email", email, "password", password, "name", name));
    }
}
