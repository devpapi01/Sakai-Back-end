package com.pfe.code.services.request;

import com.pfe.code.entities.Adresse;
import com.pfe.code.entities.Role;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Register {

    @NotBlank(message = "Le nom est obligatoire")
    @Pattern(regexp = "^[\\p{L} '-]{2,50}$", message = "Le nom ne doit contenir que des lettres, espaces, apostrophes ou tirets (2 a 50 caracteres)")
    private String nom;

    @NotBlank(message = "Le prenom est obligatoire")
    @Pattern(regexp = "^[\\p{L} '-]{2,50}$", message = "Le prenom ne doit contenir que des lettres, espaces, apostrophes ou tirets (2 a 50 caracteres)")
    private String prenom;

    @NotBlank(message = "Le telephone est obligatoire")
    @Pattern(regexp = "^\\+?[0-9 ]{6,20}$", message = "Le telephone doit etre un numero valide (6 a 20 chiffres, prefixe + optionnel)")
    private String telephone;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, max = 72, message = "Le mot de passe doit contenir entre 8 et 72 caracteres")
    private String password;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit etre une adresse valide")
    private String email;

    @NotNull(message = "L'adresse est obligatoire")
    @Valid
    private Adresse adresse;

}
