package com.example.demo.user;

import org.junit.jupiter.api.Test;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void testGetCurrentUser() throws Exception {
        UserPrincipal mockUserPrincipal = new UserPrincipal(
                9L, // Assuming an example ID
                "kacie.emard", // Username
                "kacie.emard@example.com", // Email
                "password", // Password
                "ROLE_USER" // Role
        );

        // Set up Authentication object using UserPrincipal
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                mockUserPrincipal,
                null,
                mockUserPrincipal.getAuthorities()
        );
        // Perform the request and verify the response
        mockMvc.perform(get("/api/users/me").with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("kacie.emard"));
    }

    @Test
    public void testGenerateUsers() throws Exception {
        mockMvc.perform(get("/api/users/generate")
                        .param("count", "5").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }

    @Test
    @WithMockUser(username = "kacie.emard", authorities = {"ADMIN"})
    public void testGetUserProfileAsAdmin() throws Exception {
        User mockUser = new User();
        mockUser.setUsername("raelene.gerhold");
        given(userRepository.findByUsername("raelene.gerhold")).willReturn(Optional.of(mockUser));

        mockMvc.perform(get("/api/users/{username}", "raelene.gerhold"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("raelene.gerhold"));
    }

    @Test
    public void testUploadUsers() throws Exception {
        ClassPathResource resource = new ClassPathResource("/users.json", getClass());
        MockMultipartFile file = new MockMultipartFile(
                "file", // Parameter name used in your controller method
                "users.json", // Filename
                MediaType.APPLICATION_JSON_VALUE, // Content type
                resource.getInputStream() // File content
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/users/batch")
                        .file(file)) // Add the mock file to the request
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imported").exists())
                .andExpect(jsonPath("$.notImported").exists());
    }
}
