package com.topographe.topographe.entity;

import com.topographe.topographe.entity.enumm.SkillLevel;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@DiscriminatorValue("TECHNICIEN")
public class Technicien extends User {

    @Column(name = "skill_level")
    @Enumerated(EnumType.STRING)
    private SkillLevel skillLevel;

    @Column(name = "specialties")
    private String specialties;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_topographe_id", nullable = false)
    private Topographe assignedTo;

    @OneToMany(mappedBy = "assignedTechnicien", fetch = FetchType.LAZY)
    private Set<Task> tasks = new HashSet<>();
}
