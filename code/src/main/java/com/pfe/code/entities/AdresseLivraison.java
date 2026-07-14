package com.pfe.code.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class AdresseLivraison {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le libelle est obligatoire (ex: Entrepot principal)")
    private String libelle;

    @NotBlank(message = "Le pays est obligatoire")
    private String pays;

    @NotBlank(message = "La ville est obligatoire")
    private String ville;

    @NotBlank(message = "L'emplacement est obligatoire")
    private String emplacement;

    private boolean parDefaut = false;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "marchand_id")
    private Marchand marchand;
}
