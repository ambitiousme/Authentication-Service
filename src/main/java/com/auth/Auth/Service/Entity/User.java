package com.auth.Auth.Service.Entity;

import com.auth.Auth.Service.Entity.Embedded.Address;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false,unique = true)
    private String email;

    @Column(name = "contact")
    private String contactNo;

    @Column
    private int age;

    @Column(name = "DOB")
    private LocalDate dateOfBirth;

    @Embedded
    private Address address;

    private String resetToken;

    private LocalDateTime tokenExpiry;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime creationTime;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime lastUpdateTime;

}
