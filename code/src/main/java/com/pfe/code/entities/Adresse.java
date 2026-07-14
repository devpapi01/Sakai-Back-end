package com.pfe.code.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Adresse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le pays est obligatoire")
    private String pays;

    @NotBlank(message = "La ville est obligatoire")
    private String ville;

    @NotBlank(message = "L'emplacement est obligatoire")
    private String emplacement;
    @JsonIgnore
    @OneToMany(mappedBy = "adresse")
    private List<Utilisateur> utilisateurs;

}
