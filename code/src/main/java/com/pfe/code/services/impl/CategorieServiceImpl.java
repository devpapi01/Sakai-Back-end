package com.pfe.code.services.impl;

import com.pfe.code.entities.Categorie;
import com.pfe.code.repositories.CategorieRepository;
import com.pfe.code.services.CategorieService;
import com.pfe.code.services.Exceptions.GlobalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategorieServiceImpl implements CategorieService {
    @Autowired
    CategorieRepository categorieRepository;

    @Override
    public Categorie saveCategorie(Categorie categorie) {
        return categorieRepository.save(categorie);
    }

    @Override
    public Categorie updateCategorie(Categorie categorie) {
        Categorie existing = categorieRepository.findById(categorie.getId())
                .orElseThrow(() -> new GlobalException("Categorie introuvable"));
        existing.setNom(categorie.getNom());
        existing.setDescription(categorie.getDescription());

        return categorieRepository.save(existing);
    }

    @Override
    public Categorie getCategorie(Long id) {
        return categorieRepository.findById(id)
                .orElseThrow(() -> new GlobalException("Categorie introuvable"));
    }

    @Override
    public void deleteCategorie(Long id) {

        categorieRepository.deleteById(id);

    }

    @Override
    public List<Categorie> getAll() {
        return categorieRepository.findAll();
    }

    @Override
    public List<Categorie> getByNomC(String nom) {
        return categorieRepository.findByNomContains(nom);
    }

    @Override
    public List<String> getnoms() {
        return categorieRepository.nomscats();
    }
}
