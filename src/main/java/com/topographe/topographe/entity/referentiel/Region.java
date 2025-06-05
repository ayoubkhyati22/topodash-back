package com.topographe.topographe.entity.referentiel;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "region")
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "region", nullable = false, length = 40)
    private String region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", referencedColumnName = "id")
    private Country country;

    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ville> villes = new ArrayList<>();
}
