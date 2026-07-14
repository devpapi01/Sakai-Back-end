package com.pfe.code.services;

import com.pfe.code.entities.AdresseLivraison;

import java.util.List;

public interface AdresseLivraisonService {
    List<AdresseLivraison> getForMarchand(Long marchandId);
    AdresseLivraison getByIdForMarchand(Long marchandId, Long adresseId);
    AdresseLivraison add(Long marchandId, AdresseLivraison adresse);
    AdresseLivraison update(Long marchandId, Long adresseId, AdresseLivraison adresse);
    void delete(Long marchandId, Long adresseId);
}
