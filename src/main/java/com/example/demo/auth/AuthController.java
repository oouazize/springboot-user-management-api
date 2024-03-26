package com.example.demo.auth;

import com.example.demo.security.JwtService;
import com.example.demo.user.Dto.AuthenticationRequest;
import com.example.demo.user.Dto.AuthenticationResponse;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }


    @PostMapping()
    public AuthenticationResponse authenticate(
            @RequestBody AuthenticationRequest request
    )
    {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        System.out.println(request.getUsername());
        User user = this.userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        boolean isMatch = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!isMatch) {
            throw new BadCredentialsException("Invalid password");
        }
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("Roles",user.getAuthorities());
        hm.put("username",user.getUsername());
        var jwtToken = this.jwtService.generateToken(hm,user);
        return AuthenticationResponse.builder().accessToken(jwtToken).build();
    }
}
