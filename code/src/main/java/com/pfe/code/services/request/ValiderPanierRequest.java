package com.pfe.code.services.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValiderPanierRequest {
    @NotBlank(message = "L'adresse de livraison est obligatoire")
    private String adresseLivraison;

    @NotBlank(message = "L'email du destinataire est obligatoire")
    private String emailRec;

    @NotBlank(message = "Le numero du destinataire est obligatoire")
    private String numRec;
}
