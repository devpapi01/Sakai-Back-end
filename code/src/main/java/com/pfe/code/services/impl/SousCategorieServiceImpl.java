package com.pfe.code.services.impl;

import com.pfe.code.entities.Categorie;
import com.pfe.code.entities.SousCategorie;
import com.pfe.code.repositories.SousCategorieRepository;
import com.pfe.code.services.CategorieService;
import com.pfe.code.services.Exceptions.GlobalException;
import com.pfe.code.services.SousCategorieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service


public class SousCategorieServiceImpl implements SousCategorieService {
    @Autowired
    SousCategorieRepository sousCategorieRepository;

    @Autowired
    CategorieService categorieService;

    //Ajouter l'id de la categorie courante pour attribuer a la sous categorie
    @Override
    public SousCategorie saveSousCategorie(Long id,SousCategorie sousCategorie) {
        Categorie categorie= categorieService.getCategorie(id);
        sousCategorie.setCategorie(categorie);
        return sousCategorieRepository.save(sousCategorie);
    }

    @Override
    public SousCategorie updateSousCategorie(SousCategorie sousCategorie) {
        SousCategorie existing = sousCategorieRepository.findById(sousCategorie.getId())
                .orElseThrow(() -> new GlobalException("Sous-categorie introuvable"));
        existing.setNom(sousCategorie.getNom());
        existing.setDescription(sousCategorie.getDescription());

        return sousCategorieRepository.save(existing);
    }

    @Override
    public List<SousCategorie> getByCategorieId(Long id) {
        return sousCategorieRepository.findByCategorieId(id);
    }

    @Override
    public SousCategorie getSousCategorie(Long id) {
        return sousCategorieRepository.findById(id)
                .orElseThrow(() -> new GlobalException("Sous-categorie introuvable"));
    }

    @Override
    public void deleteSousCategorie(Long id) {
sousCategorieRepository.deleteById(id);
    }

    @Override
    public List<SousCategorie> getAll() {
        return sousCategorieRepository.findAll();
    }

    @Override
    public List<SousCategorie> getByNomC(String nom) {
        return sousCategorieRepository.findByNomContains(nom);
    }

    @Override
    public List<String> getnomSC() {
        return sousCategorieRepository.nomsouscats();
    }

    @Override
    public SousCategorie getById(Long id) {
        return sousCategorieRepository.findById(id)
                .orElseThrow(() -> new GlobalException("Sous-categorie introuvable"));
    }
}
