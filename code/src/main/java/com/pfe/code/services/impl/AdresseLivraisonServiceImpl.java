package com.pfe.code.services.impl;

import com.pfe.code.entities.AdresseLivraison;
import com.pfe.code.entities.Marchand;
import com.pfe.code.repositories.AdresseLivraisonRepository;
import com.pfe.code.repositories.MarchandRepository;
import com.pfe.code.services.AdresseLivraisonService;
import com.pfe.code.services.Exceptions.GlobalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdresseLivraisonServiceImpl implements AdresseLivraisonService {

    @Autowired
    AdresseLivraisonRepository adresseLivraisonRepository;

    @Autowired
    MarchandRepository marchandRepository;

    @Override
    public List<AdresseLivraison> getForMarchand(Long marchandId) {
        return adresseLivraisonRepository.findByMarchandId(marchandId);
    }

    @Override
    public AdresseLivraison getByIdForMarchand(Long marchandId, Long adresseId) {
        AdresseLivraison adresse = adresseLivraisonRepository.findById(adresseId)
                .orElseThrow(() -> new GlobalException("Adresse de livraison introuvable"));
        if (adresse.getMarchand() == null || !adresse.getMarchand().getId().equals(marchandId)) {
            throw new GlobalException("Adresse de livraison introuvable");
        }
        return adresse;
    }

    private void clearDefaut(Long marchandId) {
        List<AdresseLivraison> existantes = adresseLivraisonRepository.findByMarchandId(marchandId);
        for (AdresseLivraison a : existantes) {
            if (a.isParDefaut()) {
                a.setParDefaut(false);
                adresseLivraisonRepository.save(a);
            }
        }
    }

    @Override
    public AdresseLivraison add(Long marchandId, AdresseLivraison adresse) {
        Marchand marchand = marchandRepository.findById(marchandId)
                .orElseThrow(() -> new GlobalException("Marchand introuvable"));

        boolean premiereAdresse = adresseLivraisonRepository.findByMarchandId(marchandId).isEmpty();
        adresse.setId(null);
        adresse.setMarchand(marchand);
        if (premiereAdresse) {
            adresse.setParDefaut(true);
        } else if (adresse.isParDefaut()) {
            clearDefaut(marchandId);
        }
        return adresseLivraisonRepository.save(adresse);
    }

    @Override
    public AdresseLivraison update(Long marchandId, Long adresseId, AdresseLivraison adresse) {
        AdresseLivraison existante = getByIdForMarchand(marchandId, adresseId);
        existante.setLibelle(adresse.getLibelle());
        existante.setPays(adresse.getPays());
        existante.setVille(adresse.getVille());
        existante.setEmplacement(adresse.getEmplacement());
        if (adresse.isParDefaut() && !existante.isParDefaut()) {
            clearDefaut(marchandId);
            existante.setParDefaut(true);
        }
        return adresseLivraisonRepository.save(existante);
    }

    @Override
    public void delete(Long marchandId, Long adresseId) {
        AdresseLivraison existante = getByIdForMarchand(marchandId, adresseId);
        boolean etaitDefaut = existante.isParDefaut();
        adresseLivraisonRepository.delete(existante);
        if (etaitDefaut) {
            List<AdresseLivraison> restantes = adresseLivraisonRepository.findByMarchandId(marchandId);
            if (!restantes.isEmpty()) {
                AdresseLivraison nouvelleDefaut = restantes.get(0);
                nouvelleDefaut.setParDefaut(true);
                adresseLivraisonRepository.save(nouvelleDefaut);
            }
        }
    }
}
