package com.pfe.code.controllers;

import com.pfe.code.entities.AdresseLivraison;
import com.pfe.code.entities.Marchand;
import com.pfe.code.security.SecurityUtils;
import com.pfe.code.services.AdresseLivraisonService;
import com.pfe.code.services.MarchandService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/marchands/adresses")
public class AdresseLivraisonRESTController {

    @Autowired
    AdresseLivraisonService adresseLivraisonService;

    @Autowired
    MarchandService marchandService;

    private Marchand currentMarchandOrDeny() {
        return marchandService.findByEmail(SecurityUtils.currentEmail())
                .orElseThrow(() -> new AccessDeniedException("Seul un marchand authentifie peut gerer ses adresses"));
    }

    @GetMapping
    public List<AdresseLivraison> getAll() {
        Marchand current = currentMarchandOrDeny();
        return adresseLivraisonService.getForMarchand(current.getId());
    }

    @PostMapping
    public AdresseLivraison add(@Valid @RequestBody AdresseLivraison adresse) {
        Marchand current = currentMarchandOrDeny();
        return adresseLivraisonService.add(current.getId(), adresse);
    }

    @PutMapping("/{id}")
    public AdresseLivraison update(@PathVariable("id") Long id, @Valid @RequestBody AdresseLivraison adresse) {
        Marchand current = currentMarchandOrDeny();
        return adresseLivraisonService.update(current.getId(), id, adresse);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        Marchand current = currentMarchandOrDeny();
        adresseLivraisonService.delete(current.getId(), id);
    }
}
