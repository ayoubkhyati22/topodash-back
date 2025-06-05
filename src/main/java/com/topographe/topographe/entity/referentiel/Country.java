package com.topographe.topographe.entity.referentiel;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "country")
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String code;

    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Region> regions = new ArrayList<>();
} 