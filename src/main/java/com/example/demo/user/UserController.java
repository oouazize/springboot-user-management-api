package com.example.demo.user;
import com.github.javafaker.Faker;
import com.google.gson.*;
import lombok.Getter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Type;

import com.google.gson.GsonBuilder;

class LocalDateAdapter implements JsonSerializer<LocalDate> {

    public JsonElement serialize(LocalDate date, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE)); // "yyyy-mm-dd"
    }
}

@Getter
class ImportSummary {

    private int imported;

    private int notImported;

    public void addImported() {
        this.imported++;
    }

    public void addNotImported() {
        this.notImported++;
    }
}

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    public UserController(UserRepository userRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/generate")
    public ResponseEntity<byte[]> generateUsers(@RequestParam int count) {
        List<User> users = new ArrayList<>();
        Faker faker = new Faker();
        for (int i = 0; i < count; i++) {
            User user = new User();
            user.setFirstName(faker.name().firstName());
            user.setLastName(faker.name().lastName());
            user.setBirthDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            user.setCity(faker.address().city());
            user.setCountry(faker.address().countryCode());
            user.setAvatar(faker.internet().avatar());
            user.setCompany(faker.company().name());
            user.setJobPosition(faker.job().title());
            user.setMobile(faker.phoneNumber().cellPhone());
            user.setUsername(faker.name().username());
            user.setEmail(faker.internet().emailAddress());
            user.setPassword(faker.internet().password(6, 10));
            user.setRole(faker.options().option(Role.ADMIN, Role.USER));
            users.add(user);
        }

        // Convert to JSON and trigger file download
        String json = gson.toJson(users);
        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"users.json\"")
                .body(jsonBytes);
    }

    @PostMapping("/batch")
    public ResponseEntity<ImportSummary> uploadUsers(@RequestPart MultipartFile file) {


        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        ImportSummary summary = new ImportSummary();

        try {

            // Read JSON from file
            String json = new String(file.getBytes());

            // Deserialize to list
            List<User> users = objectMapper.readValue(json, new TypeReference<>() {
            });

            // Process each user
            for(User user : users) {

                // Encode password
                String encodedPassword = passwordEncoder.encode(user.getPassword());
                user.setPassword(encodedPassword);


                // Check for duplicates
                if(isDuplicate(user)) {
                    summary.addNotImported();
                    continue;
                }

                // Save to database
                userRepository.save(user);

                summary.addImported();

            }
            return ResponseEntity.ok(summary);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal User userPrincipal) {
        return userRepository.findByUsername(userPrincipal.getUsername())
                .map(user -> new UserDTO(user.getId(), user.getFirstName(), user.getLastName(), user.getBirthDate(), user.getCity(), user.getCountry(), user.getAvatar(), user.getCompany(), user.getJobPosition(), user.getMobile(), user.getUsername(), user.getEmail(), user.getRole()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getUserProfile(@PathVariable String username, @AuthenticationPrincipal User userPrincipal) {

        boolean isAdmin = userPrincipal.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
        }

        return ResponseEntity.ok(userRepository.findByUsername(username)
                        .map(user -> new UserDTO(user.getId(), user.getFirstName(), user.getLastName(), user.getBirthDate(), user.getCity(), user.getCountry(), user.getAvatar(), user.getCompany(), user.getJobPosition(), user.getMobile(), user.getUsername(), user.getEmail(), user.getRole())));
    }

    private boolean isDuplicate(User user) {
        return userRepository.existsByEmailOrUsername(user.getEmail(), user.getUsername());
    }


}
