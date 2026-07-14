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
@AllArgsConstructor
@NoArgsConstructor
public class SousCategorie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long  id;

    @NotBlank(message = "Le nom de la sous-categorie est obligatoire")
    private String nom;
    private String description;
    @JsonIgnore
    @ManyToOne
    private Categorie categorie;
    @JsonIgnore
    @OneToMany(mappedBy = "sousCategorie")
    private List<Produit> produits;
}
