package com.pfe.code.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {
   @Id
   @GeneratedValue(strategy = GenerationType.SEQUENCE)
   private Long Id;
   private String nom;
   private String prenom;
   private String telephone;

   @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
   private String password;
   @Column(unique = true)
   private String email;
   @ManyToOne(cascade = CascadeType.ALL)
   @JoinColumn(name = "adresse")
   private Adresse adresse;
   @Enumerated(EnumType.STRING)
   private Role role;




}
