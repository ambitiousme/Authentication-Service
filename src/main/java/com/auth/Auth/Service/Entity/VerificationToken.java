package com.auth.Auth.Service.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class VerificationToken {
    @Id
    @GeneratedValue
    private Long id;

    private String token;

    @OneToOne
    private User user;

    private String type;

    private LocalDateTime expiryDate;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime creationTime;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime lastUpdateTime;

    public VerificationToken(User user, String token ,LocalDateTime expiryDate, String type)
    {
        this.user = user;
        this.token = token;
        this.expiryDate = expiryDate;
        this.type = type;
    }
}
