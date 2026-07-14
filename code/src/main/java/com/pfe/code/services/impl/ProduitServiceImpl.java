package com.pfe.code.services.impl;

import com.pfe.code.entities.Produit;
import com.pfe.code.repositories.ProduitRepository;
import com.pfe.code.services.Exceptions.GlobalException;
import com.pfe.code.services.ProduitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProduitServiceImpl implements ProduitService {
    @Autowired
    ProduitRepository produitRepository;
    @Override
    public List<Produit> getAll() {
        return produitRepository.findAll();
    }

    @Override
    public Produit saveProduit(Produit produit) {
        return produitRepository.save(produit);
    }

    @Override
    public Produit updateProduit(Produit produit) {
        Optional<Produit> optionalProduit= produitRepository.findById(produit.getIdProd());
        if (optionalProduit.isEmpty()) {
            throw new GlobalException("Le produit n'existe pas");
        }
        optionalProduit.get().setCategorie(produit.getCategorie());
       optionalProduit.get().setSousCategorie(produit.getSousCategorie());
       optionalProduit.get().setPrixProd(produit.getPrixProd());
       optionalProduit.get().setQuantite(produit.getQuantite());
       optionalProduit.get().setNomProd(produit.getNomProd());
       optionalProduit.get().setImages(produit.getImages());
       optionalProduit.get().setDescriptionPro(produit.getDescriptionPro());
       optionalProduit.get().setImages(produit.getImages());



        return produitRepository.save(optionalProduit.get());
    }

    @Override
    public List<Produit> findProd(String terme) {
        return produitRepository.rechercherProduits(terme);
    }

    @Override
    public Produit getProd(Long id) {
        return produitRepository.findById(id)
                .orElseThrow(() -> new GlobalException("Produit introuvable"));
    }

    @Override
    public List<Produit> getByfournisseur(Long id) {
        return produitRepository.findByFournisseurId(id);
    }

    @Override
    public List<Produit> filtre(Double minPrix, Double maxPrix, List<String> categories, List<String> souscategories, Long quantiteMin, Long quantiteMax,List<String>fournisseurs) {
        return produitRepository.filtrerProduits(minPrix,maxPrix,categories,souscategories,quantiteMin,quantiteMax,fournisseurs);
    }

    @Override
    public void deleteById(Long id) {
        produitRepository.deleteById(id);
    }

    @Override
    public List<Produit> findbynomcontains(String nom) {
        return produitRepository.findByNomProdContains(nom);
    }

    @Override
    public List<Produit> findByNomCat(String nomCat) {
        return produitRepository.findByCategorie(nomCat);
    }

    @Override
    public List<Produit> categorieAcs() {
        return produitRepository.OrderedByCategorieAsc();
    }

    @Override
    public List<Produit> categorieDesc() {
        return produitRepository.orderByCategorieDesc();
    }

    @Override
    public List<Produit> findbycategorieId(Long id) {
        return produitRepository.findByCategorieId(id);
    }

    @Override
    public List<Produit> findprixProd(Double prixProd) {
        return produitRepository.findByPrixProd(prixProd);
    }

    @Override
    public List<Produit> OrderprixA() {
        return produitRepository.OrderByPrixA();
    }

    @Override
    public List<Produit> OrderprixD() {
        return produitRepository.OrderByPrixD();
    }

    @Override
    public List<Produit> findprixbetween(Double p1, Double p2) {
        return produitRepository.findByPrixProdBetween(p1,p2);
    }

    @Override
    public List<Produit> OrderByNomasc() {
        return produitRepository.OrderByNomasc();
    }

    @Override
    public List<Produit> OrderByNomdesc() {
        return produitRepository.OrderByNomdesc();
    }
}
