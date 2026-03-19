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
import java.time.Period;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false,unique = true)
    private String email;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column(name = "contact")
    private String contactNo;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VerificationToken> verificationTokenList;

    @Transient
    public int getAge() {
        if (dob == null) return 0;
        return Period.between(dob, LocalDate.now()).getYears();
    }

    @Column(name = "DOB")
    private LocalDate dob;

    @Embedded
    private Address address;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime creationTime;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime lastUpdateTime;

}
