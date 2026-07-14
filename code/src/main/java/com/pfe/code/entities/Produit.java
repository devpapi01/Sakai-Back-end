package com.pfe.code.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Produit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProd;

    @NotBlank(message = "Le nom du produit est obligatoire")
    private String nomProd;

    @NotNull(message = "Le prix est obligatoire")
    @Positive(message = "Le prix doit etre strictement positif")
    private Double prixProd;

    private String descriptionPro;

    @NotNull(message = "La quantite est obligatoire")
    @PositiveOrZero(message = "La quantite ne peut pas etre negative")
    private Long quantite= 400L;
    private Date datecreation;

    @Version
    private Long version;


    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "fourniseur_id")
    private Fournisseur fournisseur;

    @ManyToOne
    private Categorie categorie;
    @ManyToOne
    private SousCategorie sousCategorie;

    @OneToMany(mappedBy = "produit")
    private List<Image> images;


}
