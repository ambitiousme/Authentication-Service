package com.auth.Auth.Service.DTO;

import com.auth.Auth.Service.Entity.Embedded.Address;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserResponse {
    private String username;
    private String name;
    private String email;
    private boolean emailVerified;
    private String contactNo;
    private int age;
    private LocalDate dateOfBirth;
    private Address address;
}
