package com.pfe.code.services.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValiderPanierRequest {
    // Une adresse enregistree (adresseLivraisonId) ou un texte libre
    // (adresseLivraison) doit etre fourni ; verifie par CommandeServiceImpl.
    private Long adresseLivraisonId;

    private String adresseLivraison;

    @NotBlank(message = "L'email du destinataire est obligatoire")
    private String emailRec;

    @NotBlank(message = "Le numero du destinataire est obligatoire")
    private String numRec;
}
