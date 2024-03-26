package com.example.demo.auth;

import com.example.demo.user.Dto.AuthenticationRequest;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static com.example.demo.user.Role.ADMIN;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void testAuthenticate() throws Exception {
        PasswordEncoder encoder = new BCryptPasswordEncoder();

        User mockUser = new User();
        mockUser.setUsername("kacie.emard");
        mockUser.setRole(ADMIN);
        mockUser.setPassword(encoder.encode("cjlw7z7"));
        given(userRepository.findByUsername(mockUser.getUsername())).willReturn(Optional.of(mockUser));

        AuthenticationRequest authRequest = new AuthenticationRequest("kacie.emard", "cjlw7z7");
        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)).with(csrf()))
                        .andExpect(status().isOk());
    }
}
