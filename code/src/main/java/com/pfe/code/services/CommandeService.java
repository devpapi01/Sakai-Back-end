package com.pfe.code.services;

import com.pfe.code.entities.Commande;
import com.pfe.code.entities.Etat;
import com.pfe.code.entities.PaymentStatus;

import java.util.List;
//principe SRP
public interface CommandeService {
    List<Commande> findAll();
    Commande findById(Long id);
    List<Commande>findByMarchandId(Long id);
    List<Commande>findBySLId(Long id);
    Commande findByref(String ref);
    List<Commande>findByLivreurId(Long id);
    Commande createCommande(Commande commande);
    Commande setLivreurCommande(Long idCom, Long idLivreur);
    Commande updateEtat(Long idCom,String etat);

    void deleteCommandeById(Long id);

    Commande getOrCreatePanier(Long marchandId);
    Commande getPanier(Long marchandId);
    Commande ajouterAuPanier(Long marchandId, Long produitId, Long quantite);
    Commande modifierLignePanier(Long marchandId, Long ligneId, Long quantite);
    Commande supprimerLignePanier(Long marchandId, Long ligneId);
    List<Commande> validerPanier(Long marchandId, String adresseLivraison, String emailRec, String numRec);
    Commande marquerPaiement(Long idCom, PaymentStatus status);

}
