package com.topographe.topographe.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@DiscriminatorValue("TOPOGRAPHE")
public class Topographe extends User {

    @Column(name = "license_number", unique = true)
    private String licenseNumber;

    @Column(name = "specialization")
    private String specialization;

    // Clients créés par ce topographe
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Client> clients = new HashSet<>();

    // Techniciens affectés à ce topographe
    @OneToMany(mappedBy = "assignedTo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Technicien> techniciens = new HashSet<>();

    // Projets gérés par ce topographe
    @OneToMany(mappedBy = "topographe", fetch = FetchType.LAZY)
    private Set<Project> projects = new HashSet<>();
}