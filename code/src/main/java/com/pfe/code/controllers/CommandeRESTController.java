package com.pfe.code.controllers;

import com.pfe.code.entities.Commande;
import com.pfe.code.entities.Etat;
import com.pfe.code.entities.Livreur;
import com.pfe.code.entities.Marchand;
import com.pfe.code.entities.PaymentStatus;
import com.pfe.code.entities.ServiceLivraison;
import com.pfe.code.entities.Utilisateur;
import com.pfe.code.security.SecurityUtils;
import com.pfe.code.services.CommandeService;
import com.pfe.code.services.MarchandService;
import com.pfe.code.services.UserService;
import com.pfe.code.services.request.AjouterLigneRequest;
import com.pfe.code.services.request.ModifierLigneRequest;
import com.pfe.code.services.request.ValiderPanierRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/commandes")
@RestController
public class CommandeRESTController {
    @Autowired
    CommandeService commandeService;

    @Autowired
    MarchandService marchandService;

    @Autowired
    UserService userService;

    private boolean isAdmin() {
        return SecurityUtils.hasRole("ADMIN");
    }

    private void checkIsOwnerMarchand(Long marchandId) {
        if (isAdmin()) return;
        Marchand current = marchandService.findByEmail(SecurityUtils.currentEmail()).orElse(null);
        if (current == null || marchandId == null || !current.getId().equals(marchandId)) {
            throw new AccessDeniedException("Vous n'avez pas acces a ces commandes");
        }
    }

    private void checkIsOwnerService(Long serviceId) {
        if (isAdmin()) return;
        Utilisateur current = userService.getByEmail(SecurityUtils.currentEmail());
        if (!(current instanceof ServiceLivraison) || serviceId == null || !current.getId().equals(serviceId)) {
            throw new AccessDeniedException("Vous n'avez pas acces a ces commandes");
        }
    }

    private void checkIsOwnerLivreur(Long livreurId) {
        if (isAdmin()) return;
        Utilisateur current = userService.getByEmail(SecurityUtils.currentEmail());
        if (!(current instanceof Livreur) || livreurId == null || !current.getId().equals(livreurId)) {
            throw new AccessDeniedException("Vous n'avez pas acces a ces commandes");
        }
    }

    private Marchand currentMarchandOrDeny() {
        return marchandService.findByEmail(SecurityUtils.currentEmail())
                .orElseThrow(() -> new AccessDeniedException("Seul un marchand authentifie peut gerer un panier"));
    }

    private void checkCanManageCommande(Commande commande) {
        if (isAdmin()) return;
        if (commande == null) {
            throw new AccessDeniedException("Commande introuvable");
        }
        String email = SecurityUtils.currentEmail();
        boolean isOwningService = commande.getServiceLivraison() != null
                && commande.getServiceLivraison().getEmail() != null
                && commande.getServiceLivraison().getEmail().equals(email);
        boolean isAssignedLivreur = commande.getLivreur() != null
                && commande.getLivreur().getEmail() != null
                && commande.getLivreur().getEmail().equals(email);
        if (!isOwningService && !isAssignedLivreur) {
            throw new AccessDeniedException("Vous n'avez pas acces a cette commande");
        }
    }

    @GetMapping("/all")
    public List<Commande> getAll(){
      return  commandeService.findAll();
    }

    @PostMapping("/newcommande")
    public Commande create( @RequestBody Commande commande){
        Marchand current = marchandService.findByEmail(SecurityUtils.currentEmail())
                .orElseThrow(() -> new AccessDeniedException("Seul un marchand authentifie peut passer commande"));
        commande.setMarchand(current);
        return commandeService.createCommande(commande);
    }


    @GetMapping("/getbyM/{id}")
    public List<Commande>getbyM(@PathVariable("id")Long id){
        checkIsOwnerMarchand(id);
        return commandeService.findByMarchandId(id);
    }

    @GetMapping("/getbySL/{id}")
    public List<Commande>getbySL(@PathVariable("id") Long id){
        checkIsOwnerService(id);
        return commandeService.findBySLId(id);
    }

    @GetMapping("/getbyref/{ref}")
    public Commande getbyref(@PathVariable("ref")String ref){
        return commandeService.findByref(ref);
    }
    @GetMapping("/getbylivreur/{id}")
    public List<Commande>getbyLivreur(@PathVariable("id")Long id){
        checkIsOwnerLivreur(id);
        return commandeService.findByLivreurId(id);
    }


    @PutMapping("/setlivreur/{idC}/{idL}")
    public Commande setLivreur(@PathVariable("idC")Long idC,@PathVariable("idL")Long idL){
       checkCanManageCommande(commandeService.findById(idC));
       return commandeService.setLivreurCommande(idC,idL);
    }

    @PutMapping("/etatcom/{idC}")
    public Commande setEtat(@PathVariable("idC")Long idC, @RequestBody String etat){
        checkCanManageCommande(commandeService.findById(idC));
        return commandeService.updateEtat(idC,etat);
    }
    @DeleteMapping("/deletecom/{id}")
    public void deleteCom(@PathVariable("id") Long id){
        Commande commande = commandeService.findById(id);
        checkIsOwnerMarchand(commande != null ? commande.getMarchand().getId() : null);
        commandeService.deleteCommandeById(id);
    }

    @GetMapping("/panier")
    public Commande getPanier(){
        Marchand current = currentMarchandOrDeny();
        return commandeService.getPanier(current.getId());
    }

    @PostMapping("/panier/ajouter")
    public Commande ajouterAuPanier(@Valid @RequestBody AjouterLigneRequest request){
        Marchand current = currentMarchandOrDeny();
        return commandeService.ajouterAuPanier(current.getId(), request.getProduitId(), request.getQuantite());
    }

    @PutMapping("/panier/lignes/{ligneId}")
    public Commande modifierLignePanier(@PathVariable("ligneId") Long ligneId, @Valid @RequestBody ModifierLigneRequest request){
        Marchand current = currentMarchandOrDeny();
        return commandeService.modifierLignePanier(current.getId(), ligneId, request.getQuantite());
    }

    @DeleteMapping("/panier/lignes/{ligneId}")
    public Commande supprimerLignePanier(@PathVariable("ligneId") Long ligneId){
        Marchand current = currentMarchandOrDeny();
        return commandeService.supprimerLignePanier(current.getId(), ligneId);
    }

    @PostMapping("/panier/valider")
    public List<Commande> validerPanier(@Valid @RequestBody ValiderPanierRequest request){
        Marchand current = currentMarchandOrDeny();
        return commandeService.validerPanier(current.getId(), request.getAdresseLivraison(),
                request.getEmailRec(), request.getNumRec());
    }

    @PutMapping("/{id}/payer")
    public Commande payer(@PathVariable("id") Long id){
        Commande commande = commandeService.findById(id);
        checkIsOwnerMarchand(commande != null ? commande.getMarchand().getId() : null);
        return commandeService.marquerPaiement(id, PaymentStatus.PAYEE);
    }

}
