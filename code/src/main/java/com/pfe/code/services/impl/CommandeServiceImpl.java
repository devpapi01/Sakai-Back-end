package com.pfe.code.services.impl;

import com.pfe.code.entities.*;
import com.pfe.code.repositories.*;
import com.pfe.code.services.CommandeService;
import com.pfe.code.services.Exceptions.GlobalException;
import com.pfe.code.services.MarchandService;
import com.pfe.code.services.utils.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.pfe.code.services.tools.EmailMessage.toFournisseur;

@Service

public class CommandeServiceImpl implements CommandeService {
    @Autowired
    CommandeRepository commandeRepository;

    @Autowired
    LigneCommandeRepository ligneCommandeRepository;

    @Autowired
    MarchandRepository marchandRepository;

    @Autowired
    ServiceLivraisonRepository serviceLivraisonRepository;

    @Autowired
    ProduitRepository produitRepository;

    @Autowired
    LivreurRepository livreurRepository;

    @Autowired
    EmailSender emailSender;


    public String generateCommandeReference(String nomMarchand, String nomSL,String date) {

        String m = nomMarchand.substring(0, Math.min(nomMarchand.length(), 2));
        String sl= nomSL.substring(0,Math.min(nomSL.length(), 2));

        String ref= m+sl+date;

        return ref;
    }

    @Override
    public List<Commande> findAll() {
        return commandeRepository.findAll();
    }

    @Override
    public Commande findById(Long id) {
        return commandeRepository.findById(id).orElse(null);
    }

    @Override
    public List<Commande> findByMarchandId(Long id) {
        return commandeRepository.findByMarchandId(id);
    }

    @Override
    public List<Commande> findBySLId(Long id) {
        return commandeRepository.findByServiceLivraisonId(id);
    }

    @Override
    public Commande findByref(String ref) {
        return commandeRepository.findByReference(ref);
    }

    @Override
    public List<Commande> findByLivreurId(Long id) {
        return commandeRepository.findByLivreurId(id);
    }

    @Override
    public Commande createCommande(Commande commande) {


   LocalDate date= LocalDate.now();
      java.sql.Date sqlDate = java.sql.Date.valueOf(date);


      commande.setDateCommande(sqlDate);
       String ref= generateCommandeReference(commande.getEmailRec(),commande.getNumRec(),commande.getDateCommande().toString());
       commande.setReference(ref);

       if(commande.getServiceLivraison()!=null){
           this.sendEmailUser(commande.getServiceLivraison().getEmail(),commande);
       }
      this.sendEmailUser(commande.getEmailRec(),commande);

        commande.setEtat(Etat.EN_ATTENTE);

       return commandeRepository.save(commande);

    }

    public void sendEmailUser(String email, Commande commande) {
        String emailBody ="Bonjour "+ "<h2>"+email+"</h2> "+"nouvelle commande lancée avec la reference:" +commande.getReference();

        emailSender.sendEmail(email, emailBody);
    }

    @Override
    public Commande setLivreurCommande(Long idCom, Long idLivreur) {
        Livreur livreur = livreurRepository.findById(idLivreur)
                .orElseThrow(() -> new GlobalException("Livreur introuvable"));
        Commande commande = commandeRepository.findById(idCom)
                .orElseThrow(() -> new GlobalException("Commande introuvable"));
        //email au livreur
        commande.setLivreur(livreur);
        return commandeRepository.save(commande);
    }

    @Override
    public Commande updateEtat(Long idCom, String etat) {

        //email si marchand pour l'état de sa commande
        Commande commande = commandeRepository.findById(idCom)
                .orElseThrow(() -> new GlobalException("Commande introuvable"));
        Etat etat1=Etat.fromJsonString(etat);

       commande.setEtat(etat1);


        return commandeRepository.save(commande);
    }

    @Override
    public void deleteCommandeById(Long id) {
        commandeRepository.deleteById(id);

    }

    @Override
    public Commande getOrCreatePanier(Long marchandId) {
        return commandeRepository.findFirstByMarchandIdAndEtat(marchandId, Etat.EN_PANIER)
                .orElseGet(() -> {
                    Marchand marchand = marchandRepository.findById(marchandId)
                            .orElseThrow(() -> new GlobalException("Marchand introuvable"));
                    Commande panier = new Commande();
                    panier.setMarchand(marchand);
                    panier.setEtat(Etat.EN_PANIER);
                    panier.setPaymentStatus(PaymentStatus.EN_ATTENTE);
                    panier.setPrixtotal(0.0);
                    panier.setLignesCommande(new ArrayList<>());
                    return commandeRepository.save(panier);
                });
    }

    @Override
    public Commande getPanier(Long marchandId) {
        return getOrCreatePanier(marchandId);
    }

    private Commande getPanierExistant(Long marchandId) {
        return commandeRepository.findFirstByMarchandIdAndEtat(marchandId, Etat.EN_PANIER)
                .orElseThrow(() -> new GlobalException("Aucun panier en cours"));
    }

    private LigneCommande trouverLigne(Commande panier, Long ligneId) {
        return panier.getLignesCommande().stream()
                .filter(l -> l.getId().equals(ligneId))
                .findFirst()
                .orElseThrow(() -> new GlobalException("Ligne introuvable dans le panier"));
    }

    private void recalculerTotal(Commande panier) {
        double total = panier.getLignesCommande().stream()
                .mapToLong(LigneCommande::getPrixligne)
                .sum();
        panier.setPrixtotal(total);
    }

    @Override
    public Commande ajouterAuPanier(Long marchandId, Long produitId, Long quantite) {
        Commande panier = getOrCreatePanier(marchandId);
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new GlobalException("Produit introuvable"));

        LigneCommande ligne = panier.getLignesCommande().stream()
                .filter(l -> l.getProduit().getIdProd().equals(produitId))
                .findFirst()
                .orElse(null);

        if (ligne != null) {
            ligne.setQuantite(ligne.getQuantite() + quantite);
        } else {
            ligne = new LigneCommande();
            ligne.setCommande(panier);
            ligne.setProduit(produit);
            ligne.setQuantite(quantite);
            panier.getLignesCommande().add(ligne);
        }
        ligne.setPrixligne(Math.round(produit.getPrixProd() * ligne.getQuantite()));

        recalculerTotal(panier);
        return commandeRepository.save(panier);
    }

    @Override
    public Commande modifierLignePanier(Long marchandId, Long ligneId, Long quantite) {
        Commande panier = getPanierExistant(marchandId);
        LigneCommande ligne = trouverLigne(panier, ligneId);
        ligne.setQuantite(quantite);
        ligne.setPrixligne(Math.round(ligne.getProduit().getPrixProd() * quantite));
        recalculerTotal(panier);
        return commandeRepository.save(panier);
    }

    @Override
    public Commande supprimerLignePanier(Long marchandId, Long ligneId) {
        Commande panier = getPanierExistant(marchandId);
        LigneCommande ligne = trouverLigne(panier, ligneId);
        panier.getLignesCommande().remove(ligne);
        ligneCommandeRepository.delete(ligne);
        recalculerTotal(panier);
        return commandeRepository.save(panier);
    }

    @Override
    public List<Commande> validerPanier(Long marchandId, String adresseLivraison, String emailRec, String numRec) {
        Commande panier = getPanierExistant(marchandId);
        if (panier.getLignesCommande() == null || panier.getLignesCommande().isEmpty()) {
            throw new GlobalException("Le panier est vide");
        }

        for (LigneCommande ligne : panier.getLignesCommande()) {
            Produit produit = ligne.getProduit();
            if (produit.getQuantite() < ligne.getQuantite()) {
                throw new GlobalException("Stock insuffisant pour le produit " + produit.getNomProd());
            }
        }

        Map<Long, List<LigneCommande>> lignesParFournisseur = panier.getLignesCommande().stream()
                .collect(Collectors.groupingBy(l -> l.getProduit().getFournisseur().getId()));

        LocalDate date = LocalDate.now();
        java.sql.Date sqlDate = java.sql.Date.valueOf(date);

        List<Commande> commandesCreees = new ArrayList<>();
        for (List<LigneCommande> lignes : lignesParFournisseur.values()) {
            Fournisseur fournisseur = lignes.get(0).getProduit().getFournisseur();

            Commande commande = new Commande();
            commande.setMarchand(panier.getMarchand());
            commande.setAdresseLivraison(adresseLivraison);
            commande.setEmailRec(emailRec);
            commande.setNumRec(numRec);
            commande.setDateCommande(sqlDate);
            commande.setEtat(Etat.EN_ATTENTE);
            commande.setPaymentStatus(PaymentStatus.EN_ATTENTE);
            commande.setReference(generateCommandeReference(panier.getMarchand().getNom(), fournisseur.getNom(), sqlDate.toString()));

            double totalCommande = 0;
            List<LigneCommande> nouvellesLignes = new ArrayList<>();
            for (LigneCommande ligne : lignes) {
                Produit produit = ligne.getProduit();
                // decremente le stock ; @Version sur Produit protege contre les mises a jour concurrentes
                produit.setQuantite(produit.getQuantite() - ligne.getQuantite());
                produitRepository.save(produit);

                ligne.setCommande(commande);
                if (fournisseur.getAdresse() != null) {
                    ligne.setAdrfour(fournisseur.getAdresse().getVille() + ", " + fournisseur.getAdresse().getPays());
                }
                nouvellesLignes.add(ligne);
                totalCommande += ligne.getPrixligne();
            }
            commande.setLignesCommande(nouvellesLignes);
            commande.setPrixtotal(totalCommande);

            Commande saved = commandeRepository.save(commande);
            this.sendEmailUser(fournisseur.getEmail(), saved);
            this.sendEmailUser(emailRec, saved);
            commandesCreees.add(saved);
        }

        panier.getLignesCommande().clear();
        commandeRepository.delete(panier);

        return commandesCreees;
    }

    @Override
    public Commande marquerPaiement(Long idCom, PaymentStatus status) {
        Commande commande = commandeRepository.findById(idCom)
                .orElseThrow(() -> new GlobalException("Commande introuvable"));
        commande.setPaymentStatus(status);
        return commandeRepository.save(commande);
    }
}
