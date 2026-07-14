package com.pfe.code.controllers;

import com.pfe.code.entities.Fournisseur;
import com.pfe.code.entities.Produit;
import com.pfe.code.security.SecurityUtils;
import com.pfe.code.services.FournisseurService;
import com.pfe.code.services.ProduitService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fournisseurs")
public class FournisseurRESTController {
    @Autowired
    FournisseurService fournisseurService;

    @Autowired
   ProduitService produitService;

    private void checkIsSelf(Long targetId) {
        Fournisseur current = fournisseurService.findByEmail(SecurityUtils.currentEmail()).orElse(null);
        if (current == null || !current.getId().equals(targetId)) {
            throw new AccessDeniedException("Vous ne pouvez agir que sur votre propre compte");
        }
    }

    @GetMapping("/all")
    public List<Fournisseur>getAll(){
        return fournisseurService.getAll();
    }


    @GetMapping("/allnomsf")
    public List<String>getAllnoms(){
        return fournisseurService.getAllnoms();
    }
    @GetMapping("/getid/{id}")
    public Fournisseur getByid(@PathVariable("id")Long id){
        return fournisseurService.getByid(id);
    }

    @GetMapping("/nomContains/{nom}")
    public List<Fournisseur>getByNomContains(@PathVariable("nom") String nom){
        return fournisseurService.getByNomContains(nom);
    }

    @GetMapping("/pays/{pays}")
    public List<Fournisseur>getByPays(@PathVariable("pays")String pays){
        return fournisseurService.findByAdresse_Pays(pays);
    }
    @GetMapping("/nomAcs")
    public List<Fournisseur>getByNomAcs(){
        return fournisseurService.getByNomACS();
    }

    @GetMapping("/nomDesc")
    public List<Fournisseur>getByNomDesc(){

        return fournisseurService.getByNomDESC();
    }

    @GetMapping("/preAcs")
    public List<Fournisseur>getByPreAcs(){
        return fournisseurService.getByPreAcs();
    }

    @GetMapping("/preDesc")
    public List<Fournisseur>getByPresDes(){
        return fournisseurService.getByPreDesc();
    }

    @PostMapping("/addFournisseur")
    public Fournisseur addFournisseur(@Valid @RequestBody Fournisseur fournisseur){
        return fournisseurService.saveFournisseur(fournisseur);
    }
    @DeleteMapping("/supprimerFournisseur/{id}")
    public void deleteById(@PathVariable("id") Long id){
      checkIsSelf(id);
      fournisseurService.deleteFournisseurById(id);
  }


  @PutMapping("/fouraddprod/{id}")
    public Fournisseur updateF(@PathVariable("id") Long id,@RequestBody Produit produit){
        checkIsSelf(id);
        return fournisseurService.updateFourbyid(id,produit);
  }
@PutMapping("/updateinfos")
    public Fournisseur updateinfos(@RequestBody Fournisseur fournisseuru){
     checkIsSelf(fournisseuru.getId());
     return fournisseurService.updateinfoFour(fournisseuru);
}


}
