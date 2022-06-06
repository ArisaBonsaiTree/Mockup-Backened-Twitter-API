package com.example.demo.embeddables;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@NoArgsConstructor
@Data
public class Profile {

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    // The name here is redundant as JPA will name the column the same
    // as the field by default.
    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone_number")
    private String phone;
}
