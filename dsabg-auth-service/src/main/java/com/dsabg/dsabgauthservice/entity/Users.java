package com.dsabg.dsabgauthservice.entity;

import com.dsabg.dsabgauthservice.entity.Enum.Roles;
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
    @Column(nullable = false, unique = true)
    private String username;
    private String password;
    @Column(nullable = false)
    private String email;
    private String phone;
    @Enumerated(EnumType.STRING)
    private Roles role=Roles.USER;
    private String status;
    @CreationTimestamp
    private Date createdAt;
}
