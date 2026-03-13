package com.auth.Auth.Service.DTO;

import com.auth.Auth.Service.Entity.Embedded.Address;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserRequest {

    private String name;
    private String contactNo;
    private LocalDate dateOfBirth;
    private Address address;
}
