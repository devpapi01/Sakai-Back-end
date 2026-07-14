package com.pfe.code.controllers;

import com.pfe.code.entities.Produit;
import com.pfe.code.services.ProduitService;
import com.pfe.code.services.request.ProduitFilterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/produits")
public class ProduitRESTController {
    @Autowired
    ProduitService produitService;



    @GetMapping("/allprods")
    public List<Produit> getAll(){
        return produitService.getAll();
    }


    @PostMapping("/addprod")
    public Produit addProd(@RequestBody Produit produit){
        return produitService.saveProduit(produit);
    }


    @PutMapping("/update")
   public Produit update(@RequestBody Produit produit){
        return produitService.updateProduit(produit);
    }

    @GetMapping("/filtre")
    public List<Produit>filtre(@RequestBody ProduitFilterRequest produitFilterRequest){
        return produitService.filtre(produitFilterRequest.getMinPrix(), produitFilterRequest.getMaxPrix(),
                produitFilterRequest.getCategories(),produitFilterRequest.getSouscategories(),
                produitFilterRequest.getQuantiteMin(),produitFilterRequest.getQuantiteMax(),produitFilterRequest.getFournisseurs());
    }


    @GetMapping("/search")
    public List<Produit> rechercherProduits(@RequestBody String terme) {
        return produitService.findProd(terme);
    }

    @DeleteMapping("/supprimer/{id}")
    public void delete(@PathVariable("id") Long id){
        produitService.deleteById(id);
    }

    @GetMapping("/getncont/{nom}")
   public List<Produit>getBynomc(@PathVariable("nom") String nom){
        return produitService.findbynomcontains(nom);
    }

    @GetMapping("/prodcat/{idCat}")
    public List<Produit> getProduitByCatid(@PathVariable("idCat") Long idCat){
        return produitService.findbycategorieId(idCat);
    }
    @GetMapping("/detailprod/{id}")
    public Produit getProduit(@PathVariable("id") Long id){
        return produitService.getProd(id);
    }

    @GetMapping("/prodcatnom/{nomCat}")
    public List<Produit> getProdCatn(@PathVariable("nomCat") String nomCat){
        return produitService.findByNomCat(nomCat);
    }

    @GetMapping("/fournisseur/{id}")
    public List<Produit>getByIdF(@PathVariable("id") Long id){
        return produitService.getByfournisseur(id);
    }

    @GetMapping("/prodcatacs")
    public List<Produit>getCatAcs(){
        return produitService.categorieAcs();
    }
    @GetMapping("/prix/{prix}")
    public List<Produit>getByPix(@PathVariable("prix") Double prix){
        return produitService.findprixProd(prix);
    }

    @GetMapping("/prixasc")
    public List<Produit> getByPrixAsc(){
        return produitService.OrderprixA();
    }

    @GetMapping("/prixdes")
    public List<Produit> getByPrixDesc(){
        return produitService.OrderprixD();
    }

    @GetMapping("/nomasc")
    public List<Produit>getnomasc(){
        return produitService.OrderByNomasc();
    }

    @GetMapping("/nomdesc")
    public List<Produit>getnomdesc(){
        return produitService.OrderByNomdesc();
    }





}
