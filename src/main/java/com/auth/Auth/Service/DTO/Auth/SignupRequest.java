package com.auth.Auth.Service.DTO.Auth;

import com.auth.Auth.Service.Entity.Embedded.Address;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    private String email;
    private String username;
    private String password;
    private String Name;
    private String contactNumber;
    private LocalDate dob;
    private Address address;

}
