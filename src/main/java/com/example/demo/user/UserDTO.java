package com.example.demo.user;

import java.time.LocalDate;

public record UserDTO(Long id, String firstName, String lastName, LocalDate birthDate, String city, String country,
                      String avatar, String company, String jobPosition, String mobile, String username, String email,
                      Role role) {

}
