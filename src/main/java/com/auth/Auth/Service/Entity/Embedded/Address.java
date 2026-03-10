package com.auth.Auth.Service.Entity.Embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Embeddable
public class Address {

    @Column
    private String addressLine;
    @Column
    private String city;
    @Column
    private String state;
    @Column
    private String country;
}
