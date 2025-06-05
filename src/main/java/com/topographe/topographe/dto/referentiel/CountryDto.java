package com.topographe.topographe.dto.referentiel;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryDto {
    private Long id;
    private String name;
    private String code;
} 