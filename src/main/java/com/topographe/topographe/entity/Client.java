package com.topographe.topographe.entity;

import com.topographe.topographe.entity.enumm.ClientType;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@DiscriminatorValue("CLIENT")
public class Client extends User {

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "client_type")
    @Enumerated(EnumType.STRING)
    private ClientType clientType;

    // Topographe qui a créé ce client
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_topographe_id", nullable = false)
    private Topographe createdBy;

    // Projets du client
    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    private Set<Project> projects = new HashSet<>();
}
