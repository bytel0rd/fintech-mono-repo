package com.interswitch.middleware.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = true, nullable = false)
    private String primaryAccountNumber;

    private String email;

    @Column(unique = true, nullable = false)
    private String platformId;

    @Column(nullable = false)
    private String bvn;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String dob;

    @Column(nullable = false)
    private Date createdAt;

    private Date lastLoginTime;

    private String pinHash;

}
