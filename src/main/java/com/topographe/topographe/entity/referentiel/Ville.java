package com.topographe.topographe.entity.referentiel;

import jakarta.persistence.*;

@Entity
@Table(name = "ville")
public class Ville {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ville", nullable = false, length = 40)
    private String ville;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region", referencedColumnName = "id")
    private Region region;
}
