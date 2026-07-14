package com.pfe.code.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Categorie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de la categorie est obligatoire")
    private String nom;
    private String description;

    @OneToMany(mappedBy = "categorie")
    private List<SousCategorie> sousCategorie;
     @JsonIgnore
    @OneToMany(mappedBy = "categorie")
    private List<Produit> produits;
}
