package com.pfe.code.controllers;

import com.pfe.code.entities.Livreur;
import com.pfe.code.entities.ServiceLivraison;
import com.pfe.code.entities.Utilisateur;
import com.pfe.code.security.SecurityUtils;
import com.pfe.code.services.LivreurService;
import com.pfe.code.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

@RequestMapping("/livreurs")
public class LivreurRESTController {
    @Autowired
    LivreurService livreurService;

    @Autowired
    UserService userService;

    private void checkCanManage(Livreur target) {
        String email = SecurityUtils.currentEmail();
        if (SecurityUtils.hasRole("LIVREUR")) {
            if (target == null || target.getEmail() == null || !target.getEmail().equals(email)) {
                throw new AccessDeniedException("Vous ne pouvez agir que sur votre propre compte");
            }
        } else if (SecurityUtils.hasRole("SERVICE_LIVRAISON")) {
            Utilisateur current = userService.getByEmail(email);
            if (!(current instanceof ServiceLivraison) || target == null || target.getServiceLivraison() == null
                    || !target.getServiceLivraison().getId().equals(current.getId())) {
                throw new AccessDeniedException("Ce livreur n'appartient pas a votre service");
            }
        } else {
            throw new AccessDeniedException("Action non autorisee");
        }
    }

    @GetMapping("/all")
    public List<Livreur> getAll(){
        return livreurService.getAll();
    }


    @GetMapping("/getbyid/{id}")
    public Livreur getById(@PathVariable("id") Long id){
        return livreurService.getByid(id);
    }

    @GetMapping("/getforsl/{id}")
    public List<Livreur>getLforSL(@PathVariable("id") Long id){
        return livreurService.getForServiceLivraison(id);
    }

    @PostMapping("/addlivreur/{idSL}")
    public Livreur add(@PathVariable("idSL")Long idSL, @RequestBody Livreur livreur){
        return livreurService.createLivreur(idSL,livreur);
    }

    @PutMapping("/updateL")
    public Livreur update(@RequestBody Livreur livreur){
        checkCanManage(livreurService.getByid(livreur.getId()));
        return livreurService.updateLivreur(livreur);
    }
    @DeleteMapping("/deletelivreur/{id}")
    public void delete(@PathVariable("id")Long id){
        checkCanManage(livreurService.getByid(id));
        livreurService.deleteLivreur(id);
    }


}
