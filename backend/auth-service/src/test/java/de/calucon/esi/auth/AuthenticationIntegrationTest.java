package de.calucon.esi.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.calucon.esi.auth.dto.AuthenticationRequest;
import de.calucon.esi.auth.dto.RegisterRequest;
import de.calucon.esi.auth.dto.RoleUpdateRequest;
import de.calucon.esi.auth.event.UserEventProducer;
import de.calucon.esi.auth.model.Role;
import de.calucon.esi.auth.repository.UserRepository;
import de.calucon.esi.auth.service.AuthenticationService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Rolls back db changes after each test
@TestPropertySource(properties = { "jwt.secret=b23be62e80435d3b061eb0b2cc2e655c7e056518b45a2f79839dd6e22ef86fe7" })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private UserEventProducer userEventProducer;

    // @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    private String validJwtToken;

    @BeforeEach
    void setUp() {
        // This runs before EVERY test. It ensures the database always has a user
        // with the email 'existing@user.com' and the password 'password123'.
        try {
            RegisterRequest setupRequest = RegisterRequest.builder()
                    .email("existing@user.com")
                    .password("password123")
                    .roles(Set.of(Role.DRIVER))
                    .build();

            // Register the user and save their token into our class variable
            validJwtToken = authenticationService.register(setupRequest).getToken();
            userRepository.findByEmail("existing@user.com").orElseThrow().getId();
        } catch (IllegalArgumentException e) {
            // Ignore if already exists
        }
    }

    // ==========================================
    // 1. TEST REGISTRATION
    // ==========================================
    @Test
    void testRegisterNewUser() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("brand.new@user.com")
                .password("securePassword123")
                .roles(Set.of(Role.PASSENGER))
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    // ==========================================
    // 2. TEST LOGIN
    // ==========================================
    @Test
    void testLoginExistingUser() throws Exception {
        // We use the credentials we hardcoded in the setUp() method
        AuthenticationRequest loginRequest = AuthenticationRequest.builder()
                .email("existing@user.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void testLoginWithWrongPasswordFails() throws Exception {
        AuthenticationRequest loginRequest = AuthenticationRequest.builder()
                .email("existing@user.com")
                .password("WRONG_PASSWORD")
                .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized()); // Spring Security returns 401 for bad credentials now
    }

    // ==========================================
    // 3. TEST TOKEN VALIDATION
    // ==========================================
    @Test
    void testValidateEndpointWithValidToken() throws Exception {
        // We use the token that was generated in the setUp() method
        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Token is valid"));
    }

    @Test
    void testValidateEndpointWithoutTokenFails() throws Exception {
        mockMvc.perform(get("/api/auth/validate"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testValidateEndpointWithFakeTokenFails() throws Exception {
        // A completely fabricated JWT token
        String fakeToken = "eyJhbGciOiJIUzI1NiJ9.FakePayload.FakeSignature";

        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "Bearer " + fakeToken))
                // We expect the Filter to intercept this and return a 403 Forbidden,
                // proving that our validation logic is active!
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // 4. TEST ROLES ENDPOINTS
    // ==========================================

    @Test
    void testGetUserRoles() throws Exception {
        // Send a GET request to /api/auth/roles
        mockMvc.perform(get("/api/auth/roles")
                .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                // Since we created the user as a DRIVER in setUp, we expect an array with
                // "DRIVER"
                .andExpect(jsonPath("$[0]").value("DRIVER"));
    }

    @Test
    void testUpdateUserRoles() throws Exception {
        // Create a request to change the user to BOTH a Driver and a Passenger
        RoleUpdateRequest updateRequest = new RoleUpdateRequest(Set.of(Role.DRIVER, Role.PASSENGER));

        // Send a PUT request to /api/auth/roles
        mockMvc.perform(put("/api/auth/roles")
                .header("Authorization", "Bearer " + validJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                // We expect the array length to now be 2
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ==========================================
    // 5. TEST ROLE VALIDATION ENDPOINT
    // ==========================================

    @Test
    void testHasRoleEndpointWithCorrectRole() throws Exception {
        // In setUp, existingUserId was registered with Role.DRIVER
        mockMvc.perform(get("/api/auth/validate/role/DRIVER")
                .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk());
    }

    @Test
    void testHasRoleEndpointWithIncorrectRole() throws Exception {
        // existingUserId does not have Role.PASSENGER
        mockMvc.perform(get("/api/auth/validate/role/PASSENGER")
                .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // 6. TEST TOKEN REFRESH
    // ==========================================

    @Test
    void testRefreshTokenEndpointWithValidToken() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    // ==========================================
    // 7. TEST LOGOUT
    // ==========================================

    @Test
    void testLogoutEndpoint() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out successfully"));
    }

}
