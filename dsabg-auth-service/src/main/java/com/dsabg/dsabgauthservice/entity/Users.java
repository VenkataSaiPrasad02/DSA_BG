package com.dsabg.dsabgauthservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    @Id
    private String id;
    private String username;
    private String password;
    @Column(nullable = false, unique = true)
    private String email;
    private String phone;
    private String status;
    @CreationTimestamp
    private Date createdAt;
}
