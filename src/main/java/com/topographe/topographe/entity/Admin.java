package com.topographe.topographe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
@DiscriminatorValue("ADMIN")
public class Admin extends User {
    @Column(name = "admin_level")
    private String adminLevel;
}
