package com.pfe.code.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Etat {
    EN_PANIER("Panier"),
    EN_ATTENTE("En attente"),
    EN_COURS("En cours"),
    LIVREE("Livrée"),
    ANNULEE("Annulée");

    private final String label;
    public static Etat fromString(String value) {
        for (Etat etat : Etat.values()) {
            if (etat.name().equalsIgnoreCase(value)) {
                return etat;
            }
        }
        throw new IllegalArgumentException("Invalid Etat value: " + value);
    }

    public static Etat fromJsonString(String jsonString) {
        try {
            JsonNode jsonNode = new ObjectMapper().readTree(jsonString);
            String etatString = jsonNode.get("etat").asText();
            return fromString(etatString);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON format", e);
        }
    }

    Etat(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
